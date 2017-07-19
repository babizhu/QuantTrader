package org.bbz.stock.quanttrader.trade;

import com.google.common.reflect.ClassPath;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.bbz.stock.quanttrader.consts.AllStocks;
import org.bbz.stock.quanttrader.consts.Consts;
import org.bbz.stock.quanttrader.consts.EventBusAddress;
import org.bbz.stock.quanttrader.consts.JsonConsts;
import org.bbz.stock.quanttrader.trade.core.OrderCost;
import org.bbz.stock.quanttrader.trade.core.QuantTradeContext;
import org.bbz.stock.quanttrader.trade.model.ITradeModel;
import org.bbz.stock.quanttrader.trade.stockdata.IStockDataProvider;
import org.bbz.stock.quanttrader.trade.stockdata.impl.TuShareDataProvider;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
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

    private void init() throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, ClassNotFoundException{
        ClassPath classpath = ClassPath.from( Thread.currentThread().getContextClassLoader() ); // scans the class path used by classloader
        for( ClassPath.ClassInfo classInfo : classpath.getTopLevelClassesRecursive( "org.bbz.stock.quanttrader.trade.model.impl" ) ) {
//            System.out.println( classInfo.getName() );
            if( classInfo.load().getSuperclass().equals( org.bbz.stock.quanttrader.trade.model.AbstractTradeModel.class ) ) {
                tradeModelMap.put( classInfo.getSimpleName(), classInfo.load() );
            }
//            System.out.print( classInfo.getName() + "::::::");
//            Arrays.stream( classInfo.load().getInterfaces() ).forEach( System.out::println );
        }

//        System.out.println( tradeModelMap );
        Class<?> clazz = tradeModelMap.get( "WaveTradeModel" );
        Constructor c = clazz.getConstructor( QuantTradeContext.class, IStockDataProvider.class );
        final JsonObject argument = new JsonObject().put( JsonConsts.CTX_KEY, new JsonObject().put( JsonConsts.INIT_BALANCE_KEY, "100000" ) );
        argument.put( JsonConsts.MODEL_CLASS_KEY, "WaveTradeModel" );
        System.out.println( argument );
        final ITradeModel tradeModel = createTradeModel( argument );
        tradeModel.beforeOpen();

        tradeModel.run( 343434344L );
//        vertx.setPeriodic( 300000,tradeModel::run );
//        c.newInstance( null );
    }

    /**
     * 创建ITradeModel实例
     *
     * @param argument 相关参数
     */
    private ITradeModel createTradeModel( JsonObject argument ) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException{
        QuantTradeContext ctx = createQuantTradeContext( argument.getJsonObject( JsonConsts.CTX_KEY ) );
        final IStockDataProvider dataProvider = createDataProvider( argument.getJsonObject( JsonConsts.DATA_PROVIDER_KEY ) );

        String className = getClassName( argument.getString( JsonConsts.MODEL_CLASS_KEY ) );
        Class<?> clazz = Class.forName( className );
        Constructor c = clazz.getConstructor( QuantTradeContext.class, IStockDataProvider.class );
        return (ITradeModel) c.newInstance( ctx, dataProvider );
    }

    /**
     * 获取完整的类名（包括包名）
     *
     * @param simpleClassName 简单的类名称
     * @return 添加包路径的类名称
     */
    private String getClassName( String simpleClassName ){
        String packageName = simpleClassName.toLowerCase().replace( "model", "" );
        return Consts.TRADE_MODEL_CLASS_PREFIX + packageName + "." + simpleClassName;
    }

    private IStockDataProvider createDataProvider( JsonObject dataProvider ){
        final HttpClientOptions httpClientOptions = new HttpClientOptions();
        httpClientOptions.setDefaultPort( 8888 ).setDefaultHost( "localhost" ).setConnectTimeout( 4000 ).setKeepAlive( true );
        return TuShareDataProvider.createShare( null, vertx.createHttpClient( httpClientOptions ) );

    }

    /**
     * 生成QuantTradeContext实例
     *
     * @param ctxJson json内容
     * @return QuantTradeContext
     */
    private QuantTradeContext createQuantTradeContext( JsonObject ctxJson ){
        final BigDecimal closeTax = new BigDecimal( ctxJson.getString( "closeTax", "0.001" ) );
        final BigDecimal openCommission = new BigDecimal( ctxJson.getString( "openCommission", "0.0003" ) );
        final BigDecimal closeCommission = new BigDecimal( ctxJson.getString( "closeCommission", "0.0003" ) );
        final BigDecimal minCommission = new BigDecimal( ctxJson.getString( "minCommission", "5" ) );
        final OrderCost orderCost = new OrderCost( closeTax, openCommission, closeCommission, minCommission );
        final String initBalance = ctxJson.getString( JsonConsts.INIT_BALANCE_KEY, JsonConsts.DEFAULT_INIT_BALANCE_VALUE );
        final QuantTradeContext ctx = new QuantTradeContext( orderCost, initBalance );
        final HashMap<String, Integer> stocks = new HashMap<>();
        final Map<String, String> allStocks = AllStocks.INSTANCE.getAllStocks();
        for( String s : allStocks.keySet() ) {
//            stocks.put( s, 0 );
        }

//        000889 : 买入
//        600239 : 买入
//        300436 : 买入
//        002006 : 买入
//
//        000012 : 买入
//        stocks.put( "000889", 0 );
//        stocks.put( "600239", 0 );
//        stocks.put( "300436", 0 );
////        stocks.put( "000546", 0 );
//        stocks.put( "002006", 0 );
        stocks.put( "000012", 0 );

//        stocks.put( "000031", 0 );
//        stocks.put( "601318", 0 );
//        stocks.put( "000568", 0 );
//        stocks.put( "002340", 0 );
        ctx.getPortfolio().setStocks( stocks );
        return ctx;
    }
}