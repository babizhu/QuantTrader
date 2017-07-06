package org.bbz.stock.quanttrader.model.impl.wavetrade;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.redis.RedisClient;
import org.bbz.stock.quanttrader.core.OrderCost;
import org.bbz.stock.quanttrader.core.QuantTradeContext;
import org.bbz.stock.quanttrader.stockdata.RedisDataProvider;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liulaoye on 17-7-6.
 * WaveTrideModelTest
 */
public class WaveTrideModelTest{
    @Test
    public void run() throws Exception{
        final Vertx vertx = Vertx.vertx();
        final RedisClient redisClient = RedisClient.create( vertx );
        final HttpClientOptions httpClientOptions = new HttpClientOptions();
        httpClientOptions.setDefaultPort( 8888 ).setDefaultHost( "localhost" ).setConnectTimeout( 4000 ).setKeepAlive( true );
        RedisDataProvider.create( redisClient, vertx.createHttpClient( httpClientOptions ) );
        QuantTradeContext ctx = new QuantTradeContext( new OrderCost(), "10" );
        Map<String, Integer> stockMap = new HashMap<>();

        stockMap.put( "600848", 0 );

        ctx.getPortfolio().setStocks( stockMap );
        final WaveTrideModel model = new WaveTrideModel( ctx, RedisDataProvider.INSTANCE() );
        model.run( 232323L );

        Thread.sleep( 100000000 );
    }

}