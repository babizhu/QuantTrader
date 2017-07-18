package org.bbz.stock.quanttrader.trade;

import com.google.common.reflect.ClassPath;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.bbz.stock.quanttrader.consts.EventBusAddress;
import org.bbz.stock.quanttrader.trade.core.QuantTradeContext;
import org.bbz.stock.quanttrader.trade.stockdata.IStockDataProvider;

import java.io.IOException;
import java.lang.reflect.Constructor;
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
    private Map<String, Class<?>> tradeModelMap = new HashMap<>();

    public void start( Future<Void> startFuture ) throws Exception{
        final EventBus eventBus = vertx.eventBus();
        address = EventBusAddress.TRADE_MODEL_ADDR + index.getAndAdd( 1 );
        eventBus.consumer( address, this::onMessage );
        log.info( "TradeVerticle Started completed. Listen on " + address );
        init();
    }

    private void onMessage( Message<JsonObject> message ){

        System.out.println( address + "----------" + Thread.currentThread().getName() );
    }

    private void init() throws IOException, NoSuchMethodException{
        ClassPath classpath = ClassPath.from( Thread.currentThread().getContextClassLoader() ); // scans the class path used by classloader
        for( ClassPath.ClassInfo classInfo : classpath.getTopLevelClassesRecursive( "org.bbz.stock.quanttrader.trade.model.impl" ) ) {
            System.out.println( classInfo.getName() );
            if( classInfo.load().getSuperclass().equals( org.bbz.stock.quanttrader.trade.model.AbstractTradeModel.class ) ) {
                tradeModelMap.put( classInfo.getSimpleName(), classInfo.load() );
            }
//            System.out.print( classInfo.getName() + "::::::");
//            Arrays.stream( classInfo.load().getInterfaces() ).forEach( System.out::println );
        }

        System.out.println( tradeModelMap );
        Class<?> clazz = tradeModelMap.get( "WaveTrideModel" );
        Constructor c = clazz.getConstructor( QuantTradeContext.class, IStockDataProvider.class );
//        c.newInstance( null );
    }

    /**
     * 创建ITradeModel实例
     *
     * @param argument 相关参数
     */
    private void createTradeModel( JsonObject argument ) throws ClassNotFoundException{
        Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass( "" );
    }
}
