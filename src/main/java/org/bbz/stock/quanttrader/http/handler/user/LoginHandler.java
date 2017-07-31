package org.bbz.stock.quanttrader.http.handler.user;

import com.google.common.base.Strings;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTOptions;
import io.vertx.ext.auth.mongo.AuthenticationException;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import org.bbz.stock.quanttrader.consts.ErrorCode;
import org.bbz.stock.quanttrader.consts.EventBusAddress;
import org.bbz.stock.quanttrader.consts.EventBusCommand;
import org.bbz.stock.quanttrader.consts.JsonConsts;
import org.bbz.stock.quanttrader.http.handler.AbstractHandler;
import org.bbz.stock.quanttrader.http.utils.CustomHashStrategy;

/**
 * 负责处理股票交易相关接口
 * 2017-07-24 16:20
 * author:liulaoye
 */
@Slf4j
public class LoginHandler extends AbstractHandler{

    private final JWTAuth jwtAuthProvider;

    public LoginHandler( EventBus eventBus, JWTAuth jwtAuthProvider ){
        super( eventBus );
        this.jwtAuthProvider = jwtAuthProvider;

    }

    @Override
    public Router addRouter( Router restAPI ){

        restAPI.route( "/login" ).handler( this::login );

        return restAPI;
    }

    private void login( RoutingContext ctx ){
        HttpServerRequest request = ctx.request();

        String username = request.getParam( "username" );
        if( Strings.isNullOrEmpty( username ) ) {
            reportError( ctx, ErrorCode.PARAMETER_ERROR, "username is null" );
            return;
        }
        String password = request.getParam( "password" );
        if( Strings.isNullOrEmpty( password ) ) {
            reportError( ctx, ErrorCode.PARAMETER_ERROR, "password is null" );
            return;
        }
        JsonObject usernameJson = new JsonObject().put( JsonConsts.USER_NAME, username );
        DeliveryOptions options = new DeliveryOptions().addHeader( "action", EventBusCommand.DB_USER_QUERY.name() );

        send( EventBusAddress.DB_ADDR, usernameJson, options, ctx, reply -> {
            final ErrorCode errorCode = checkUserLogin( (JsonArray) reply.body(), password );
            if( errorCode.isSuccess() ) {
                String token = jwtAuthProvider.generateToken(
                        new JsonObject()
                                .put( "username", username)
                                .put( "roles", new JsonArray().add( "admin" ) ),
                        new JWTOptions()
                                .setSubject( "Quant Trade" )
                                .setIssuer( "bbz company" ) );
                ctx.response().putHeader( "Authorization", "Bearer " + token );
                reportSuccessMsg( ctx, token );
            } else {
                reportError( ctx, errorCode );
            }
        } );
    }



    private boolean examinePassword( String password, String storedPassword, String salt ){
        String cryptPassword =CustomHashStrategy.INSTANCE.cryptPassword( password, salt );
        return storedPassword != null && storedPassword.equals( cryptPassword );
    }

    private ErrorCode checkUserLogin( JsonArray resultList, String password )
            throws AuthenticationException{
        switch( resultList.size() ) {
            case 0: {
//                String message = "No account found for user [" + authToken.username + "]";
                // log.warn(message);
//                throw new AuthenticationException(message);
                return ErrorCode.USER_NOT_FOUND;
            }
            case 1: {
                JsonObject json = resultList.getJsonObject( 0 );

                if( examinePassword( password, json.getString( JsonConsts.USER_PASSWORD ), json.getString( JsonConsts.USER_SALT ) ) )
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
                throw new AuthenticationException( "怎么查出来多个用户" );
            }
        }
    }




}
