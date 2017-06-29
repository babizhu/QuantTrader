package org.bbz.stock.quanttrader.stockdata;

import io.vertx.redis.RedisClient;

/**
 * Created by liulaoye on 17-6-29.
 * 提供股票数据
 * reids版本
 */
public class RedisDataProvider{
//    INSTANCE;

    private final RedisClient redis;

    public RedisDataProvider( RedisClient redis ){
        this.redis = redis;
    }

    public static void main( String[] args ){

    }


}
