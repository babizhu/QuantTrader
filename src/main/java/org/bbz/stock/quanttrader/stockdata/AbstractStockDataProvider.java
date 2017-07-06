package org.bbz.stock.quanttrader.stockdata;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisClient;
import org.bbz.stock.quanttrader.tradehistory.SimpleKBar;

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
     * @param code
     * @param kType
     * @param count
     * @param resultHandler
     */
    @Override
    public void getSimpleKBarExt( String code, String kType, int count, Handler<AsyncResult<List<SimpleKBar>>> resultHandler ){
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
}