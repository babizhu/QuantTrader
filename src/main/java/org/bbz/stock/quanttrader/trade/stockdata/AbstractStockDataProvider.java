package org.bbz.stock.quanttrader.trade.stockdata;

import io.vertx.core.http.HttpClient;
import io.vertx.redis.RedisClient;

/**
 * Created by liulaoye on 17-7-6.
 * AbstractStockDataProvider
 */
public abstract class AbstractStockDataProvider implements IStockDataProvider{
    protected final RedisClient redis;
    protected final HttpClient httpClient;

    public AbstractStockDataProvider( RedisClient redis, HttpClient httpClient ){
        this.redis = redis;
        this.httpClient = httpClient;

    }


}