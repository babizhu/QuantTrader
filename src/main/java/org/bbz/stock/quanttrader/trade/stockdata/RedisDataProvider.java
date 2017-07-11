package org.bbz.stock.quanttrader.trade.stockdata;

import io.vertx.core.http.HttpClient;
import io.vertx.redis.RedisClient;

/**
 * Created by liulaoye on 17-6-29.
 * 最新数据还是通过tushare来抓取
 * 提供股票数据
 * reids版本
 */
public class RedisDataProvider extends AbstractStockDataProvider{
    private static RedisDataProvider provider;

    public static void create( RedisClient redis, HttpClient httpClient ){

        provider = new RedisDataProvider( redis, httpClient );
    }

    public static RedisDataProvider INSTANCE(){
        return provider;
    }


    private RedisDataProvider( RedisClient redis, HttpClient httpClient ){
        super( redis, httpClient );
    }


    public static void main( String[] args ){

    }
}
