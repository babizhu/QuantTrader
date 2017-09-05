package org.bbz.stock.quanttrader.http.handler.user;

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTOptions;
import io.vertx.ext.auth.mongo.AuthenticationException;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import org.bbz.stock.quanttrader.consts.*;
import org.bbz.stock.quanttrader.http.handler.AbstractHandler;
import org.bbz.stock.quanttrader.http.utils.CustomHashStrategy;
import org.bbz.stock.quanttrader.util.Base64Utils;
import org.bbz.stock.quanttrader.util.RSAUtils;

/**
 * 负责处理股票交易相关接口
 * 2017-07-24 16:20
 * author:liulaoye
 */
@Slf4j
public class LoginHandler extends AbstractHandler {


    private final JWTAuth jwtAuthProvider;

    public LoginHandler(EventBus eventBus, JWTAuth jwtAuthProvider) {
        super(eventBus);
        this.jwtAuthProvider = jwtAuthProvider;

    }

    @Override
    public Router addRouter(Router restAPI) {

        restAPI.route("/login").handler(this::login);
        restAPI.route("/logout").handler(this::logout);

        return restAPI;
    }

    private void logout(RoutingContext ctx) {
        ctx.response().end();
    }

    private void login(RoutingContext ctx) {
//        HttpServerRequest request = ctx.request();
//
//        MultiMap params = request.params();
//        for( Map.Entry<String, String> param : params ) {
//            System.out.println(param.getKey() + " \t" + param.getValue());
//        }
//        String username = request.getParam( "username" );
//        if( Strings.isNullOrEmpty( username ) ) {
//            reportError( ctx, ErrorCode.PARAMETER_ERROR, "username is null" );
//            return;
//        }
//        String password = request.getParam( "password" );
//        if( Strings.isNullOrEmpty( password ) ) {
//            reportError( ctx, ErrorCode.PARAMETER_ERROR, "password is null" );
//            return;
//        }
        JsonObject userJson = ctx.getBodyAsJson();
        String username = userJson.getString(JsonConsts.USER_NAME);
        if (username == null) {
            reportError(ctx, ErrorCode.PARAMETER_ERROR, "username is null");
            return;
        }
        String password = userJson.getString(JsonConsts.USER_PASSWORD);
        if (password == null) {
            reportError(ctx, ErrorCode.PARAMETER_ERROR, "password is null");
            return;
        }
        String p = decodeRsaPassword(password);
        JsonObject usernameJson = new JsonObject().put(JsonConsts.USER_NAME, username);
        DeliveryOptions options = new DeliveryOptions().addHeader("action", EventBusCommand.DB_USER_QUERY.name());

        send(EventBusAddress.DB_ADDR, usernameJson, options, ctx, reply -> {
            final ErrorCode errorCode = checkUserLogin((JsonArray) reply.body(), p);
            if (errorCode.isSuccess()) {
                String token = jwtAuthProvider.generateToken(
                        new JsonObject()
                                .put("success", true)
                                .put("message", "ok")
                                .put("username", username)
                                .put("roles", new JsonArray().add("admin")),
                        new JWTOptions()
                                .setSubject("Quant Trade")
                                .setIssuer("bbz company"));
                ctx.response().putHeader("Authorization", "Bearer " + token);
                reportSuccessMsg(ctx, token);
            } else {
                reportError(ctx, errorCode);
            }
        });
    }

    /**
     * 解码从客户端传过来的经过非对称加密之后的密码
     *
     * @param password 密码的密文
     * @return 密码明文
     */
    private String decodeRsaPassword(String password) {
        try {
            final byte[] decode = Base64Utils.decode(password);
            return new String(RSAUtils.decryptByPrivateKey(decode, RSAKey.PRIVATE_KEY));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean examinePassword(String password, String storedPassword, String salt) {
        String cryptPassword = CustomHashStrategy.INSTANCE.cryptPassword(password, salt);
        return storedPassword != null && storedPassword.equals(cryptPassword);
    }

    private ErrorCode checkUserLogin(JsonArray resultList, String password)
            throws AuthenticationException {
        switch (resultList.size()) {
            case 0: {
//                String message = "No account found for user [" + authToken.username + "]";
                // log.warn(message);
//                throw new AuthenticationException(message);
                return ErrorCode.USER_NOT_FOUND;
            }
            case 1: {
                JsonObject json = resultList.getJsonObject(0);

                if (examinePassword(password, json.getString(JsonConsts.USER_PASSWORD), json.getString(JsonConsts.USER_SALT)))
                    return ErrorCode.SUCCESS;
                else {
//                    String message = "Invalid username/password [" + authToken.username + "]";
//                    // log.warn(message);
//                    throw new AuthenticationException(message);
                    return ErrorCode.USER_UNAME_PASS_INVALID;
                }
            }
            default: {
                // More than one row returned!
//                String message = "More than one user row found for user [" + authToken.username + "( "
//                        + resultList.result().size() + " )]. Usernames must be unique.";
                // log.warn(message);
                throw new AuthenticationException("怎么查出来多个用户");
            }
        }
    }


}
