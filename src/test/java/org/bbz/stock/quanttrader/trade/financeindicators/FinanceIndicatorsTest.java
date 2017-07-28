package org.bbz.stock.quanttrader.trade.financeindicators;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.bbz.stock.quanttrader.trade.tradehistory.SimpleKBar;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by liukun on 2017/7/3.
 * FinanceIndicatorsTest
 */
public class FinanceIndicatorsTest{

    private List<Double> closes600848 = new ArrayList<>();
    private List<Double> closes002769 = new ArrayList<>();
    private List<Double> closes002770 = new ArrayList<>();
    private List<SimpleKBar> dayK002770 = new ArrayList<>();
    private List<SimpleKBar> dayK600848 = new ArrayList<>();
    private HttpClient httpClient;

    @Test
    public void getSimpleKBarExtTest() throws InterruptedException{

        int count = 2;
        Future.<List<SimpleKBar>>future( f ->
                getSimpleKBarExt( "002769", "W", count, f )
        ).compose( res -> {

            if( weekUp( res ) ) {
                return Future.future( f -> getSimpleKBarExt( "002769", "D", 1, f ) );
            } else {
                Future<List<SimpleKBar>> failResult = Future.failedFuture( "周线未上摆" );
                return failResult;
            }
        } ).compose( res -> {
            if( dValueGreaterThan( res, 35 ) ) {
                Future<List<SimpleKBar>> failResult = Future.failedFuture( "60分钟K值 大于 35" );
                return failResult;
            } else {
                return Future.future( f -> getSimpleKBarExt( "002769", "D", 1, f ) );
            }
        } ).setHandler( res -> {
            if( res.failed() ) {
                res.cause().printStackTrace();
            } else {
                System.out.println( "setHandler: " + res.result() );
            }
        } );

        Thread.sleep( 100000 );
    }

    /**
     * 检测D值是否大于某个数值
     *
     * @param data k线序列
     * @param v    要比较的值
     * @return true:   大于参数v
     * false:  小于参数v
     */
    private boolean dValueGreaterThan( List<SimpleKBar> data, double v ){
        System.out.println( "FinanceIndicatorsTest.dValueGreaterThan:" + data );
        return true;
    }

    private boolean weekUp( List<SimpleKBar> data ){
        System.out.println( "FinanceIndicatorsTest.weekUp:" + data );
        return true;
    }

    private void getSimpleKBarExt( String code, String kType, int count, Handler<AsyncResult<List<SimpleKBar>>> resultHandler ){
//        Future.<Message<String>>future( f ->
//                vertx.eventBus().send("address1", "message", f)
//        )
        String uri = "/?code=" + code + "&&ktype=" + kType + "&&count=" + count;
        final HttpClientRequest request = httpClient.get( uri, resp -> {
            resp.exceptionHandler( exception -> {
                Future<List<SimpleKBar>> failResult = Future.failedFuture( exception );
                resultHandler.handle( failResult );
            } );
            resp.bodyHandler( body -> {
                final JsonArray result = body.toJsonArray();
                List<SimpleKBar> data = new ArrayList<>();
                for( Object o : result ) {
                    final JsonObject jo = (JsonObject) o;
                    data.add( new SimpleKBar( LocalDateTime.parse( jo.getString( "date" ) ),jo.getDouble( "open" ), jo.getDouble( "high" )
                            , jo.getDouble( "low" ), jo.getDouble( "close" )
                            , 100 ) );
                }
                Future<List<SimpleKBar>> successResult = Future.succeededFuture( data );
                resultHandler.handle( successResult );
            } );
        } );
        request.exceptionHandler( System.out::println ).end();
    }

    /**
     * 通过web接口从tushare返回需要的数据
     * code：股票代码，即6位数字代码，或者指数代码（sh=上证指数 sz=深圳成指 hs300=沪深300指数 sz50=上证50 zxb=中小板 cyb=创业板）
     * start：开始日期，格式YYYY-MM-DD
     * end：结束日期，格式YYYY-MM-DD
     * ktype：数据类型，D=日k线 W=周 M=月 5=5分钟 15=15分钟 30=30分钟 60=60分钟，默认为D
     * retry_count：当网络异常后重试次数，默认为3
     * pause:重试时停顿秒数，默认为0
     */
    private void getSimpleKBar( String code, String kType, int count, Handler<JsonArray> resultHandler ){
        String uri = "/?code=" + code + "&&ktype=" + kType + "&&count=" + count;
        final HttpClientRequest request = httpClient.get( uri, resp -> resp.bodyHandler( body -> {
            final JsonArray result = body.toJsonArray();

//            System.out.println( result );
            resultHandler.handle( result );
        } ) );
        request.exceptionHandler( System.out::println )
                .end();
    }

    /**
     * 卢根炒股大法
     * 1、周线上摆
     * 2、60分钟K值<35
     * 3、60分钟k线形成上摆
     *
     * @throws InterruptedException InterruptedException
     */
    @Test
    public void calcKDJwithHour() throws InterruptedException{
        int count = 100;
        getSimpleKBar( "600077", "60", count, result -> {

                    List<SimpleKBar> data = new ArrayList<>();
                    for( Object o : result ) {
                        final JsonObject jo = (JsonObject) o;
                        data.add( new SimpleKBar(LocalDateTime.parse( jo.getString( "date" ) ), jo.getDouble( "open" ), jo.getDouble( "high" )
                                , jo.getDouble( "low" ), jo.getDouble( "close" )
                                , 100 ) );
                    }
//                    Collections.reverse( data );
//                    System.out.println(data);
                    try {
                        double[][] doubles = FinanceIndicators.INSTANCE.calcKDJ( data, 8, 3, 2 );
                        int len = data.size() - 1;
                        System.out.println( "K:" + doubles[0][len] + ", D:" + doubles[1][len] + ", J:" + doubles[2][len] );
                    } catch( Exception e ) {
                        e.printStackTrace();
                    }

//                    System.out.println( Arrays.toString( doubles[2] ) );

                }
        );

        Thread.sleep( 100000 );
    }


    @Test
    public void calcKDJ() throws Exception{


//        dayK002770.save( new SimpleKBar( LocalDateTime.now(),5.34, 5.47, 5.34, 5.39, 100 ) );
        List<SimpleKBar> simpleDayKBars = dayK600848.subList( 0, dayK002770.size() );
//        System.out.println( simpleDayKBars.size() );
//        System.out.println( simpleDayKBars );
        double[][] doubles = FinanceIndicators.INSTANCE.calcKDJ( simpleDayKBars, 8, 3, 2 );
        int len = simpleDayKBars.size() - 1;
        System.out.println( "K:" + doubles[0][len] + ", D:" + doubles[1][len] + ", J:" + doubles[2][len] );
        System.out.println( Arrays.toString( doubles[2] ) );

//        getSimpleKBar( "600848", );
    }


    @Before
    public void prepareData(){
        Vertx vertx = Vertx.vertx();
        final HttpClientOptions httpClientOptions = new HttpClientOptions();
        httpClientOptions.setDefaultPort( 8888 ).setDefaultHost( "localhost" ).setConnectTimeout( 4000 )
                .setKeepAlive( true );
        httpClient = vertx.createHttpClient( httpClientOptions );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),19.97, 21.2, 19.68, 20.89, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),20.55, 21.1, 20.3, 20.65, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),20.52, 20.82, 20.42, 20.67, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),20.72, 20.81, 20.42, 20.45, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),20.38, 20.98, 20.27, 20.79, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),20.75, 21.49, 20.7, 21.23, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),21.5, 22.29, 21.3, 21.78, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),21.57, 22.38, 21.5, 22.35, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),22.17, 22.26, 21.42, 21.59, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),21.46, 21.8, 21.3, 21.59, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),21.7, 21.7, 21.22, 21.33, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),21.35, 21.39, 20.73, 20.88, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),20.99, 22.82, 20.89, 22.22, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),22.08, 22.18, 21.61, 21.93, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),21.82, 21.93, 21.5, 21.53, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),21.52, 21.87, 21.52, 21.63, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),21.61, 22.48, 21.61, 22.08, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),22, 22.08, 21.71, 21.79, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),21.79, 22.44, 21.68, 21.94, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),22.13, 22.2, 21.75, 21.98, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),21.88, 21.94, 21.3, 21.41, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),21.3, 21.38, 21.02, 21.1, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),21.17, 21.19, 20.84, 20.93, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),20.96, 21.29, 20.89, 21.11, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),20.92, 20.99, 20, 20.78, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),20.52, 21.2, 20.52, 20.89, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),20.8, 20.87, 20.57, 20.58, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),20.6, 20.78, 20.5, 20.57, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),20.6, 20.78, 20.32, 20.42, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),20.4, 20.62, 20.21, 20.48, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),20.47, 20.55, 20.18, 20.34, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),20.19, 20.24, 19.88, 19.89, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),19.88, 20.16, 19.28, 19.5, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),19.5, 20.17, 19.35, 19.96, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),19.91, 20.27, 19.64, 19.76, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),19.78, 19.84, 19.54, 19.7, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),19.8, 19.94, 19.56, 19.67, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),19.68, 20.17, 19.63, 19.91, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),19.9, 19.96, 19.61, 19.68, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),19.73, 21.58, 19.73, 20.99, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),21, 21.26, 20.65, 20.81, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),20.81, 22.36, 20.81, 21.91, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),21.3, 21.4, 19.72, 20.73, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),20.36, 20.86, 20.03, 20.66, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),20.26, 22.66, 20.26, 21.41, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),21.3, 21.8, 21.02, 21.43, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),21.21, 21.42, 20.51, 20.66, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),20.67, 21.54, 20.43, 20.84, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),20.61, 21.78, 20.52, 21.47, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),21.3, 21.53, 20, 20.23, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),20.07, 20.16, 19.21, 19.5, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),19.49, 19.74, 19.38, 19.63, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),19.58, 19.58, 18.7, 18.72, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),18.88, 19.05, 18.7, 18.75, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),18.85, 19, 18.55, 18.8, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),18.9, 18.92, 18.26, 18.74, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),18.87, 19.5, 18.75, 19.45, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),19.2, 19.76, 19.1, 19.38, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),19.28, 19.65, 19.11, 19.35, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),19.31, 19.35, 19.01, 19.11, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),19.1, 19.24, 18.5, 18.64, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),18.58, 18.85, 18.45, 18.52, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),18.53, 18.83, 18.48, 18.72, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),18.63, 18.8, 18.27, 18.32, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),18.35, 18.35, 17.51, 17.92, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),17.85, 17.97, 17.13, 17.6, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),17.3, 17.76, 17.3, 17.51, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),17.5, 17.71, 17.18, 17.7, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),17.87, 18.29, 17.75, 17.93, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),17.8, 18.09, 17.75, 17.8, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),17.81, 18.07, 17.8, 17.85, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),17.85, 18.06, 17.69, 17.74, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),17.75, 17.99, 17.36, 17.41, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),17.2, 17.41, 16.72, 16.82, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),16.95, 17.15, 16.84, 16.99, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),17.1, 17.95, 17, 17.77, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),17.85, 18.25, 17.8, 17.91, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),17.9, 18.24, 17.73, 17.94, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),18.15, 18.19, 17.65, 18.18, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),18.16, 18.94, 18.14, 18.69, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),18.74, 18.8, 18.4, 18.55, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),18.59, 19.44, 18.59, 19.3, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),19.35, 19.5, 18.93, 19, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),19.2, 19.2, 18.6, 19.07, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),18.8, 18.8, 18.1, 18.14, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),18.25, 19.35, 18.25, 19.12, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),19.31, 19.91, 19.11, 19.3, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),19.33, 20.35, 19.33, 20.21, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),20.41, 21.78, 20.41, 20.8, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),21, 22.85, 20.94, 22.5, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),23.75, 24.45, 22.22, 24.08, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),23, 23.84, 22.9, 23.57, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),23.32, 25.93, 23.26, 25.93, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),24, 24.47, 23.34, 23.34, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),23.3, 23.87, 22.57, 23.26, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),23.1, 23.24, 22.06, 22.2, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),22.2, 22.37, 21.41, 21.67, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),21.78, 21.94, 21.47, 21.74, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),21.6, 21.74, 21.13, 21.56, 100 ) );
        dayK600848.add( new SimpleKBar( LocalDateTime.now(),21.59, 22.14, 21.37, 21.98, 100 ) );

        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.774, 5.805, 5.716, 5.748, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.769, 5.774, 5.685, 5.763, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.763, 5.826, 5.763, 5.774, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.795, 5.821, 5.753, 5.784, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.795, 5.947, 5.753, 5.884, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.952, 5.952, 5.837, 5.853, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.832, 5.905, 5.763, 5.774, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.727, 5.8, 5.7, 5.779, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.774, 5.774, 5.674, 5.69, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.695, 5.947, 5.679, 5.874, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.837, 5.916, 5.816, 5.858, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.853, 5.9, 5.837, 5.879, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.889, 5.895, 5.832, 5.853, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.837, 5.868, 5.811, 5.863, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.858, 5.868, 5.79, 5.79, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.737, 5.811, 5.737, 5.795, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.774, 5.868, 5.769, 5.826, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.816, 5.863, 5.548, 5.658, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.658, 5.716, 5.569, 5.679, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.658, 5.947, 5.658, 5.916, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.889, 6.509, 5.858, 6.404, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),6.22, 6.273, 6.01, 6.057, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),6.084, 6.194, 6, 6.068, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),6.01, 6.057, 5.931, 5.989, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.963, 5.973, 5.858, 5.973, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.973, 6.057, 5.905, 5.916, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.879, 5.942, 5.863, 5.879, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.884, 5.952, 5.868, 5.937, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.931, 5.968, 5.826, 5.826, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.837, 5.853, 5.758, 5.811, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.811, 5.9, 5.784, 5.858, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.816, 5.847, 5.763, 5.784, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.805, 5.842, 5.748, 5.811, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.8, 5.832, 5.753, 5.779, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.79, 5.805, 5.721, 5.721, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.7, 5.784, 5.7, 5.727, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.727, 5.753, 5.627, 5.637, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.616, 5.648, 5.354, 5.375, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.412, 5.48, 5.38, 5.454, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.464, 5.553, 5.464, 5.548, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.553, 5.559, 5.496, 5.522, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.511, 5.58, 5.475, 5.548, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.69, 5.732, 5.548, 5.595, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.606, 5.679, 5.564, 5.669, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.664, 5.7, 5.632, 5.669, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.648, 5.732, 5.606, 5.685, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.669, 5.706, 5.595, 5.637, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.648, 5.669, 5.564, 5.637, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.637, 5.648, 5.417, 5.417, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.422, 5.669, 5.275, 5.496, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.422, 5.491, 5.407, 5.449, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.422, 5.47, 5.407, 5.422, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.422, 5.449, 5.239, 5.249, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.223, 5.312, 5.223, 5.254, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.265, 5.333, 5.239, 5.281, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.286, 5.323, 5.207, 5.317, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.323, 5.359, 5.265, 5.359, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.365, 5.433, 5.359, 5.365, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.365, 5.417, 5.323, 5.396, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.38, 5.428, 5.338, 5.396, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.386, 5.417, 5.344, 5.391, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.354, 5.417, 5.307, 5.396, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.553, 5.653, 5.517, 5.611, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.517, 5.585, 5.344, 5.491, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.396, 5.396, 5.118, 5.317, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.254, 5.333, 5.233, 5.323, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.291, 5.328, 5.275, 5.281, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.286, 5.323, 5.17, 5.317, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.312, 5.386, 5.302, 5.344, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.328, 5.359, 5.265, 5.275, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.286, 5.312, 5.228, 5.281, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.286, 5.296, 5.139, 5.155, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.155, 5.165, 4.84, 4.861, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),4.855, 5.008, 4.782, 4.966, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),4.934, 5.102, 4.913, 5.065, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.107, 5.17, 5.097, 5.165, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.207, 5.223, 5.155, 5.186, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.123, 5.165, 4.939, 4.945, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),4.939, 5.107, 4.913, 5.071, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.086, 5.212, 5.071, 5.17, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.113, 5.223, 5.113, 5.186, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.197, 5.312, 5.16, 5.312, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.365, 5.449, 5.307, 5.328, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.333, 5.344, 5.275, 5.338, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.291, 5.302, 5.207, 5.218, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.233, 5.38, 5.207, 5.359, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.354, 5.359, 5.244, 5.281, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.27, 5.42, 5.27, 5.33, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.37, 5.52, 5.35, 5.38, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.34, 5.39, 5.28, 5.34, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.35, 5.39, 5.3, 5.34, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.34, 5.36, 5.21, 5.25, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.24, 5.26, 5.13, 5.13, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.11, 5.26, 5.08, 5.21, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.21, 5.47, 5.17, 5.37, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.35, 5.43, 5.28, 5.38, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.33, 5.41, 5.27, 5.32, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.37, 5.37, 5.25, 5.27, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.27, 5.27, 5.16, 5.23, 100 ) );
        dayK002770.add( new SimpleKBar( LocalDateTime.now(),5.23, 5.37, 5.2, 5.34, 100 ) );
        double[] data600848 = {
                21.98,
                21.56,
                21.74,
                21.67,
                22.2,
                23.26,
                23.34,
                25.93,
                23.57,
                24.08,
                22.5,
                20.8,
                20.21,
                19.3,
                19.12,
                18.14,
                19.07,
                19.0,
                19.3,
                18.55,
                18.69,
                18.18,
                17.94,
                17.91,
                17.77,
                16.99,
                16.82,
                17.41,
                17.74,
                17.85,
                17.8,
                17.93,
                17.7,
                17.51,
                17.6,
                17.92,
                18.32,
                18.72,
                18.52,
                18.64,
                19.11,
                19.35,
                19.38,
                19.45,
                18.74,
                18.8,
                18.75,
                18.72,
                19.63,
                19.5,
                20.23,
                21.47,
                20.84,
                20.66,
                21.43,
                21.41,
                20.66,
                20.73,
                21.91,
                20.81,
                20.99,
                19.68,
                19.91,
                19.67,
                19.7,
                19.76,
                19.96,
                19.5,
                19.89,
                20.34,
                20.48,
                20.42,
                20.57,
                20.58,
                20.89,
                20.78,
                21.11,
                20.93,
                21.1,
                21.41,
                21.98,
                21.94,
                21.79,
                22.08,
                21.63,
                21.53,
                21.93,
                22.22,
                20.88,
                21.33,
                21.59,
                21.59,
                22.35,
                21.78,
                21.23,
                20.79,
                20.45,
                20.67,
                20.65,
                20.89

        };


        for( int i = data600848.length; i > 0; i-- ) {
            closes600848.add( data600848[i - 1] );
        }
        double[] data002769 = {
                19.22,
                21.138,
                21.317,
                20.651,
                20.79,
                20.631,
                20.413,
                21.267,
                20.651,
                21.029,
                21.407,
                21.387,
                21.138,
                22.013,
                22.311,
                22.45,
                21.834,
                21.566,
                21.327,
                22.013,
                21.744,
                21.526,
                21.069,
                21.069,
                21.198,
                20.989,
                20.592,
                20.77,
                20.631,
                20.413,
                20.572,
                20.095,
                19.737,
                19.628,
                19.717,
                20.015,
                19.767,
                19.19,
                19.28,
                19.946,
                19.826,
                19.618,
                19.071,
                19.379,
                19.28,
                19.28,
                18.942,
                18.634,
                18.654,
                18.475,
                18.087,
                17.888,
                16.944,
                16.924,
                16.994,
                17.054,
                17.103,
                16.994,
                16.915,
                16.805,
                16.815,
                15.98,
                16.02,
                15.225,
                14.818,
                14.718,
                14.788,
                15.523,
                15.414,
                15.633,
                15.384,
                14.649,
                13.844,
                13.814,
                13.943,
                13.993,
                13.993,
                13.168,
                13.754,
                15.126,
                16.02,
                16.07,
                15.901,
                15.79,
                15.37,
                15.91,
                15.78,
                16.07,
                15.93,
                15.8,
                15.75,
                15.88,
                15.5,
                15.15,
                15.21,
                15.34,
                14.97,
                15.13,
                15.2,
                16.72,
        };

        for( double v : data002769 ) {
            closes002769.add( v );
        }

        double[] data002770 = {
                5.748,
                5.763,
                5.774,
                5.784,
                5.884,
                5.853,
                5.774,
                5.779,
                5.69,
                5.874,
                5.858,
                5.879,
                5.853,
                5.863,
                5.79,
                5.795,
                5.826,
                5.658,
                5.679,
                5.916,
                6.404,
                6.057,
                6.068,
                5.989,
                5.973,
                5.916,
                5.879,
                5.937,
                5.826,
                5.811,
                5.858,
                5.784,
                5.811,
                5.779,
                5.721,
                5.727,
                5.637,
                5.375,
                5.454,
                5.548,
                5.522,
                5.548,
                5.595,
                5.669,
                5.669,
                5.685,
                5.637,
                5.637,
                5.417,
                5.496,
                5.449,
                5.422,
                5.249,
                5.254,
                5.281,
                5.317,
                5.359,
                5.365,
                5.396,
                5.396,
                5.391,
                5.396,
                5.611,
                5.491,
                5.317,
                5.323,
                5.281,
                5.317,
                5.344,
                5.275,
                5.281,
                5.155,
                4.861,
                4.966,
                5.065,
                5.165,
                5.186,
                4.945,
                5.071,
                5.17,
                5.186,
                5.312,
                5.328,
                5.338,
                5.218,
                5.359,
                5.281,
                5.33,
                5.38,
                5.34,
                5.34,
                5.25,
                5.13,
                5.21,
                5.37,
                5.38,
                5.32,
                5.27,
                5.23,
                5.34,
        };
        for( double v : data002770 ) {
            closes002770.add( v );
        }
//        System.out.println( closes002769 );
//        System.out.println( closes002769.size() );
    }

    @Test
    public void calcEXPMA() throws Exception{

//        List<Double> data = closes002769;
        List<Double> data = closes600848;
        Double expma = FinanceIndicators.INSTANCE.calcEXPMA( data, 12 );
        System.out.println( expma );
        expma = FinanceIndicators.INSTANCE.calcEXPMA( data, 5 );
        System.out.println( expma );
    }


    @Test
    public void calcMACD() throws Exception{
        List<Double> data = closes002770;
//        System.out.println( FinanceIndicators.INSTANCE.calcMACD( data,5,21,8 ));
//        System.out.println( FinanceIndicators.INSTANCE.calcMACD( data,5,8,21 ));
//        System.out.println( FinanceIndicators.INSTANCE.calcMACD( data,21,8,5 ));
//        System.out.println( FinanceIndicators.INSTANCE.calcMACD( data,21,5,8 ));
//        System.out.println( FinanceIndicators.INSTANCE.calcMACD( data,8,5,21 ));
        System.out.println( FinanceIndicators.INSTANCE.calcMACD( data, 8, 21, 5 ) );
    }

}