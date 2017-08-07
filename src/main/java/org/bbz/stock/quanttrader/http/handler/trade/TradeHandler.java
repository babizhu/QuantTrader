package org.bbz.stock.quanttrader.http.handler.trade;

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import org.bbz.stock.quanttrader.consts.EventBusAddress;
import org.bbz.stock.quanttrader.consts.EventBusCommand;
import org.bbz.stock.quanttrader.consts.JsonConsts;
import org.bbz.stock.quanttrader.http.handler.AbstractHandler;
import org.bbz.stock.quanttrader.http.handler.auth.anno.RequirePermissions;
import org.bbz.stock.quanttrader.http.handler.auth.anno.RequireRoles;

/**
 * 负责处理股票交易相关接口
 * 2017-07-24 16:20
 * author:liulaoye
 */
@Slf4j
public class TradeHandler extends AbstractHandler{

    public TradeHandler( EventBus eventBus ){
        super( eventBus );

    }

    @Override
    public Router addRouter( Router restAPI ){
        restAPI.route( "/startTrade" ).handler( this::startTrade );
        restAPI.route( "/getTradeInfo" ).handler( this::getTradeInfo );

        return restAPI;
    }

    /**
     * 查看策略的运行情况
     */
    @RequirePermissions("sys:trade:query")
    @RequireRoles( "user")
    private void getTradeInfo( RoutingContext ctx ){
        DeliveryOptions options = new DeliveryOptions().addHeader( "action", EventBusCommand.TRADE_GET_INFO.name() );
        final int taskId = Integer.parseInt( ctx.request().getParam( "taskId" ));
        final JsonObject msg = new JsonObject().put( "taskId", taskId );
        send( EventBusAddress.TRADE_MODEL_ADDR + "0", msg, options, ctx, reply -> {

            final JsonObject body = (JsonObject) reply.body();
            String res = "<meta http-equiv=\"refresh\" content=\"10\">"+body.getString( "res" ) + "<br/><br/><br/><br/><br/><br/><br/>";
            res += "<h3>L-L(ver 1.0)必发财炒股鸡</h3>";
//
            ctx.response().setStatusCode( 200 )
                    .putHeader( "content-type", "text/html; charset=utf-8" ).end( res );
            log.info( res );

        } );
    }

    /**
     * 开始运行一个策略
     */
    private void startTrade( RoutingContext ctx ){
        String stocks = "600023,600166,600200,600361,600482,600489,600498,600722,600787,601000,601928,601929,000034,000401,002146,002373,002375,002467,002477,002657";
        final JsonObject msg = new JsonObject().put( JsonConsts.CTX_KEY,
                new JsonObject()
                        .put( JsonConsts.INIT_BALANCE_KEY, "100000" )
                        .put( JsonConsts.STOCK_LIST_KEY,stocks ));
        msg.put( JsonConsts.MODEL_CLASS_KEY, "WaveTradeModel" );
        msg.put( "taskId", Integer.parseInt( ctx.request().getParam( "taskId" ) ) );
        System.out.println( msg );

        DeliveryOptions options = new DeliveryOptions().addHeader( "action", EventBusCommand.TRADE_START.name() );

        send( EventBusAddress.TRADE_MODEL_ADDR + "0", msg, options, ctx, reply -> ctx.response().end( "startTrade success" ) );
    }
}
