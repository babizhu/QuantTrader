package org.bbz.stock.quanttrader.http.handler.trade;

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
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

        return restAPI;
    }

    private void delUser( RoutingContext routingContext ){
    }

    /**
     * 添加或者修改用户信息
     */
    private void saveUser( RoutingContext ctx ){
        DeliveryOptions options = new DeliveryOptions().addHeader( "action", EventBusCommand.DB_ADD_USER.name() );
        final JsonObject msg = new JsonObject().put( "name", "liulaoye" ).put( "password","123456" );
        send( EventBusAddress.DB_ADDR, msg, options, ctx, reply -> {
            final String  id = (String) reply.body();
            ctx.response().end( id );
        } );
    }

}
