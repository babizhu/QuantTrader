package org.bbz.stock.quanttrader.http.handler.user;

import com.google.common.base.Strings;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import org.bbz.stock.quanttrader.consts.EventBusAddress;
import org.bbz.stock.quanttrader.consts.EventBusCommand;
import org.bbz.stock.quanttrader.http.handler.AbstractHandler;
import org.bbz.stock.quanttrader.http.handler.auth.anno.RequirePermissions;

/**
 * 负责处理股票交易相关接口
 * 2017-07-24 16:20
 * author:liulaoye
 */
@Slf4j
public class AuthHandler extends AbstractHandler{

    public AuthHandler( EventBus eventBus ){
        super( eventBus);
    }

    @Override
    public Router addRouter( Router restAPI ){
        restAPI.route( "/MapperFromDB" ).handler( this::saveRole );
        restAPI.route( "/del" ).handler( this::delUser );
        restAPI.route( "/query" ).handler( this::query );



        return restAPI;
    }


    @RequirePermissions("sys:user:query")
    private void query( RoutingContext ctx ){
        JsonObject condition = new JsonObject();
        HttpServerRequest request = ctx.request();
        String role = request.getParam( "role" );
        if( !Strings.isNullOrEmpty( role ) ) {

            condition.put( "role", role );
        }

        DeliveryOptions options = new DeliveryOptions().addHeader( "action", EventBusCommand.DB_ROLE_QUERY.name() );

        send( EventBusAddress.DB_ADDR, condition, options, ctx, reply ->
                ctx.response().end( reply.body().toString() ) );
    }

    private void delUser( RoutingContext routingContext ){
    }

    /**
     * 添加或者修改角色信息
     */
    private void saveRole( RoutingContext ctx ){
        DeliveryOptions options = new DeliveryOptions().addHeader( "action", EventBusCommand.DB_ROLE_SAVE.name() );
        final JsonObject msg = new JsonObject().put( "role","system").
                put(  "permissions","sys/user/MapperFromDB,sys/user/del,sys/user/query" );
        send( EventBusAddress.DB_ADDR, msg, options, ctx, reply -> ctx.response().end( reply.body().toString() ) );
    }


}
