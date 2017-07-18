package org.bbz.stock.quanttrader.trade;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.bbz.stock.quanttrader.consts.EventBusAddress;
import org.bbz.stock.quanttrader.trade.model.ITradeModel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by liulaoye on 17-7-17.
 * TradeVerticle
 */
@SuppressWarnings("unused")
@Slf4j
public class TradeVerticle extends AbstractVerticle{

    private static AtomicInteger index = new AtomicInteger( 0 );
    private String address;
    /**
     * 属于本线程的策略模型实例
     */
    private Map<Integer, ITradeModel> tradeModelMap = new HashMap<>();
    public void start( Future<Void> startFuture ) throws Exception{
        final EventBus eventBus = vertx.eventBus();
        address = EventBusAddress.TRADE_MODEL_ADDR + index.getAndAdd( 1 );
        eventBus.consumer( address, this::onMessage );
        log.info( "TradeVerticle Started completed. Listen on " + address );
    }

    private void onMessage( Message<JsonObject> message ){
        System.out.println(address +"----------" +  Thread.currentThread().getName() );
    }
}
