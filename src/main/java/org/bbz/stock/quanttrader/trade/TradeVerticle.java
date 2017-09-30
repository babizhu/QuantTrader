package org.bbz.stock.quanttrader.trade;

import static org.bbz.stock.quanttrader.consts.Consts.TRADE_MODEL_CLASS_PREFIX;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.bbz.stock.quanttrader.consts.ErrorCode;
import org.bbz.stock.quanttrader.consts.ErrorCodeException;
import org.bbz.stock.quanttrader.consts.EventBusAddress;
import org.bbz.stock.quanttrader.consts.EventBusCommand;
import org.bbz.stock.quanttrader.consts.JsonConsts;
import org.bbz.stock.quanttrader.trade.core.OrderCost;
import org.bbz.stock.quanttrader.trade.core.QuantTradeContext;
import org.bbz.stock.quanttrader.trade.model.ITradeModel;
import org.bbz.stock.quanttrader.trade.stockdata.IStockDataProvider;
import org.bbz.stock.quanttrader.trade.stockdata.impl.TuShareDataProvider;

/**
 * Created by liulaoye on 17-7-17.
 * TradeVerticle
 */
//@SuppressWarnings("unused")
@Slf4j
public class TradeVerticle extends AbstractVerticle{

    private static AtomicInteger index = new AtomicInteger( 0 );

//    保存属于本verticle(线程)的策略模型实例

//    private Map<String, Class<?>> tradeModelClassMap = new HashMap<>();
    /**
     * 正在运行的策略任务
     */
    private Map<String, ITradeModel> tradeModelTaskMap = new HashMap<>();

    public void start( Future<Void> startFuture ) throws Exception{
        final EventBus eventBus = vertx.eventBus();
        String address = EventBusAddress.TRADE_MODEL_ADDR + index.getAndAdd( 1 );
        eventBus.consumer( address, this::onMessage );
        log.info( "TradeVerticle Started completed. Listen on " + address );
//        init();
    }

    /**
     * @param message message
     */
    private void onMessage( Message<JsonObject> message ){
        if( !message.headers().contains( "action" ) ) {
            message.fail( ErrorCode.NOT_IMPLENMENT.toNum(), "No action header specified" );
        }
        String action = message.headers().get( "action" );
        JsonObject arguments = message.body();
        JsonObject result = null;
        try {
            switch( EventBusCommand.valueOf( action ) ) {
                case TRADE_START:
                    start( arguments );
                    break;
                case TRADE_GET_INFO:
                    result = getTradeInfo( arguments );
                    break;
                default:
                    message.fail( ErrorCode.BAD_ACTION.toNum(), "Bad action: " + action );
            }
        } catch( ErrorCodeException e ) {
            message.fail( e.getErrorCode(), e.getMessage() );
            e.printStackTrace();
            return;
        } catch( Exception e ) {
            message.fail( ErrorCode.SYSTEM_ERROR.toNum(), e.toString() );
            e.printStackTrace();
            return;
        }
        //确保所有的调用都是同步返回的，否则下面的代码就没有意义
        if( result != null ) {
            message.reply( result );
        } else {
            message.reply( ErrorCode.SUCCESS.toNum() );
        }
    }

    private JsonObject getTradeInfo( JsonObject arguments ){
        final String  id = arguments.getString( JsonConsts.MONGO_DB_ID );
        final ITradeModel tradeModel = tradeModelTaskMap.get( id );

        if( tradeModel == null ) {
            throw new ErrorCodeException( ErrorCode.PARAMETER_ERROR, id );
        }
        String lastInfo = tradeModel.getTradeInfo();
        return new JsonObject().put( "res", lastInfo );
    }

//    private void init() throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, ClassNotFoundException{
//        ClassPath classpath = ClassPath.from( Thread.currentThread().getContextClassLoader() ); // scans the class path used by classloader
//        for( ClassPath.ClassInfo classInfo : classpath.getTopLevelClassesRecursive( TRADE_MODEL_CLASS_PREFIX ) ) {
//            if( classInfo.load().getSuperclass().equals( org.bbz.stock.quanttrader.trade.model.AbstractTradeModel.class ) ) {
//                tradeModelClassMap.put( classInfo.getSimpleName(), classInfo.load() );
//            }
//        }
//    }

    /**
     * 通过json配置信息启动一个策略模型
     *
     * @param argument 配置参数
     */
    private void start( JsonObject argument ) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException{

        final String  id = argument.getString( JsonConsts.MONGO_DB_ID );
        final ITradeModel tradeModel = createTradeModel( argument );
        tradeModelTaskMap.put( id, tradeModel );
        vertx.setPeriodic( 30000, tradeModel::run );
    }

    /**
     * 创建ITradeModel实例
     *
     * @param argument 相关参数
     */
    private ITradeModel createTradeModel( JsonObject argument ) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException{
        QuantTradeContext ctx = createQuantTradeContext( argument.getJsonObject( JsonConsts.CTX_KEY ) );
        final IStockDataProvider dataProvider = createDataProvider( argument.getJsonObject( JsonConsts.DATA_PROVIDER_KEY ) );

        String tradeModelClassName = getClassName( argument.getString( JsonConsts.MODEL_CLASS_KEY ) );
        Class<?> clazz = Class.forName( tradeModelClassName );
        Constructor c = clazz.getConstructor( QuantTradeContext.class, IStockDataProvider.class );
        return (ITradeModel) c.newInstance( ctx, dataProvider );
    }


    /**
     * 获取完整的类名（包括完整包名）
     *
     * @param simpleClassName 简单的类名称
     * @return 添加包路径的类名称
     */
    private String getClassName( String simpleClassName ){
        String packageName = simpleClassName.toLowerCase().replace( "model", "" );
        return TRADE_MODEL_CLASS_PREFIX + "." + packageName + "." + simpleClassName;
    }

    private IStockDataProvider createDataProvider( JsonObject dataProvider ){
        final HttpClientOptions httpClientOptions = new HttpClientOptions();
        String host = "localhost";
        if( dataProvider != null ){
            host = dataProvider.getString( "host", "localhost" );
        }
        httpClientOptions.setDefaultPort( 8888 ).setDefaultHost( host ).setConnectTimeout( 4000 ).setKeepAlive( true );
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
        final String stockList = ctxJson.getString( JsonConsts.STOCKS);
        final HashMap<String, Integer> stocks = new HashMap<>();

//        final Map<String, String> allStocks = AllStocks.INSTANCE.getAllStocks();
//        for( String s : allStocks.keySet() ) {
////            stocks.put( s, 0 );, 0 );
//        }

        for( String stock : stockList.split( "," ) ) {
            stocks.put( stock, 0 );
        }
//        stocks.put( "600332", 0 );
//        stocks.put( "000999", 0 );
//        stocks.put( "601607", 0 );
//        stocks.put( "000776", 0 );
//        stocks.put( "601555", 0 );
//        stocks.put( "000036", 0 );
//        stocks.put( "600067", 0 );
//        stocks.put( "600325", 0 );
//        stocks.put( "000965", 0 );
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
//        stocks.put( "000012", 0 );

//        stocks.put( "000031", 0 );
//        stocks.put( "601318", 0 );
//        stocks.put( "000568", 0 );
//        stocks.put( "002340", 0 );
        ctx.getPortfolio().setStocks( stocks );
        return ctx;
    }
}
