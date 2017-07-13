package org.bbz.stock.quanttrader.trade.stockdata;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisClient;
import org.bbz.stock.quanttrader.consts.KLineType;
import org.bbz.stock.quanttrader.trade.tradehistory.SimpleKBar;
import org.bbz.stock.quanttrader.util.DateUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liulaoye on 17-7-6.
 * AbstractStockDataProvider
 */
public abstract class AbstractStockDataProvider implements IStockDataProvider{
    private final RedisClient redis;
    private final HttpClient httpClient;

    public AbstractStockDataProvider( RedisClient redis, HttpClient httpClient ){
        this.redis = redis;
        this.httpClient = httpClient;

    }

    /**
     *
     * @param stockId               股票ID
     * @param kLineType             K线类型
     * @param count                 数量
     * @param resultHandler         回调地狱
     */
    @Override
    public void getSimpleKBarExt( String stockId, KLineType kLineType, int count, Handler<AsyncResult<List<SimpleKBar>>> resultHandler ){
//        String uri = "/?code=" + stockId + "&&ktype=" + kLineType.toStr() + "&&count=" + count;
        getSimpleKBarExt( stockId,kLineType,count,null,null,resultHandler );
    }


    @Override
    public void getSimpleKBarExt( String stockId, KLineType kLineType, int count,
                           LocalDate start,
                           LocalDate end,
                           Handler<AsyncResult<List<SimpleKBar>>> resultHandler ){
        String uri = "/?code=" + stockId + "&&ktype=" + kLineType.toStr() + "&&count=" + count;
        if( start != null && end != null ){
            uri += "&&start=" + DateUtil.formatDate( start ) + "&&end=" + DateUtil.formatDate( end );
        }
        final HttpClientRequest request = httpClient.get( uri, resp -> {
            resp.exceptionHandler( exception -> {
                Future<List<SimpleKBar>> failResult = Future.failedFuture( exception );
                resultHandler.handle( failResult );
            } );
            resp.bodyHandler( body -> {
                final JsonArray result = body.toJsonArray();
                if(result.size() == 0){
                    Future<List<SimpleKBar>> failResult = Future.failedFuture( "股票信息不存在" );
                    resultHandler.handle( failResult );
                    return;
                }
                List<SimpleKBar> data = new ArrayList<>();
                for( Object o : result ) {
                    final JsonObject jo = (JsonObject) o;
                    data.add( new SimpleKBar( jo.getDouble( "open" ), jo.getDouble( "high" )
                            , jo.getDouble( "low" ), jo.getDouble( "close" )
                            , 100 ) );
                }
                Future<List<SimpleKBar>> successResult = Future.succeededFuture( data );
                resultHandler.handle( successResult );
            } );
        } );
        request.exceptionHandler( System.out::println ).end();
    }


    @Override
    public void getCurrentPrice( String stockId, Handler<AsyncResult<Double>> resultHandler ){

    }
}