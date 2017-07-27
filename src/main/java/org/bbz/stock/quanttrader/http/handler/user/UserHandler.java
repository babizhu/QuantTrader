package org.bbz.stock.quanttrader.http.handler.user;

import com.google.common.base.Strings;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import org.bbz.stock.quanttrader.consts.EventBusAddress;
import org.bbz.stock.quanttrader.consts.EventBusCommand;
import org.bbz.stock.quanttrader.http.handler.AbstractHandler;

/**
 * 负责处理股票交易相关接口
 * 2017-07-24 16:20
 * author:liulaoye
 */
@Slf4j
public class UserHandler extends AbstractHandler{

    public UserHandler( EventBus eventBus ){
        super( eventBus );
    }

    @Override
    public Router addRouter( Router restAPI ){
        restAPI.route( "/save" ).handler( this::saveUser );
        restAPI.route( "/del" ).handler( this::delUser );
        restAPI.route( "/query" ).handler( this::query );

        return restAPI;
    }

    private void query( RoutingContext ctx ){
        JsonObject condition = new JsonObject();
        HttpServerRequest request = ctx.request();
        String name = request.getParam( "name" );
        if( !Strings.isNullOrEmpty( name ) ) {

            condition.put( "name", name );
        }
        String id = request.getParam( "id" );
        if( !Strings.isNullOrEmpty( id ) ) {

            condition.put( "_id", id );
        }

        DeliveryOptions options = new DeliveryOptions().addHeader( "action", EventBusCommand.DB_USER_QUERY.name() );

        send( EventBusAddress.DB_ADDR, condition, options, ctx, reply ->
                ctx.response().end( reply.body().toString() ) );
    }

    private void delUser( RoutingContext routingContext ){
    }

    /**
     * 添加或者修改用户信息
     */
    private void saveUser( RoutingContext ctx ){
        DeliveryOptions options = new DeliveryOptions().addHeader( "action", EventBusCommand.DB_USER_ADD.name() );
        final JsonObject msg = new JsonObject().put( "name", "liulaoye" ).put( "password", "123456" );
        send( EventBusAddress.DB_ADDR, msg, options, ctx, reply -> {
            final String id = (String) reply.body();
            ctx.response().end( id );
        } );
    }

}
