package org.bbz.stock.quanttrader.trade.model.impl.wavetrade;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.redis.RedisClient;
import org.bbz.stock.quanttrader.trade.core.OrderCost;
import org.bbz.stock.quanttrader.trade.core.QuantTradeContext;
import org.bbz.stock.quanttrader.trade.stockdata.impl.TuShareDataProvider;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by liulaoye on 17-7-6.
 * WaveTrideModelTest
 */
public class WaveTrideModelTest{
    WaveTrideModel model;
    @Test
    public void calcCleanPriceInBigWave() throws Exception{
        model.calcCleanPriceInBigWave( "600352" );

        Thread.sleep( 100000000 );
//        WaveTrideModel.( "" );
    }

    @Before
    public void init(){
        final Vertx vertx = Vertx.vertx();
        final RedisClient redisClient = RedisClient.create( vertx );
        final HttpClientOptions httpClientOptions = new HttpClientOptions();
        httpClientOptions.setDefaultPort( 8888 ).setDefaultHost( "localhost" ).setConnectTimeout( 4000 ).setKeepAlive( true );
        final TuShareDataProvider provider = TuShareDataProvider.createShare( redisClient, vertx.createHttpClient( httpClientOptions ) );
        QuantTradeContext ctx = new QuantTradeContext( new OrderCost(), "10" );
        Map<String, Integer> stockMap = new HashMap<>();

        String STOCK_ID ="002491";
        ctx.order( STOCK_ID, 1000);
        stockMap.put( STOCK_ID, 1000 );

        ctx.getPortfolio().setStocks( stockMap );
        model = new WaveTrideModel( ctx, provider );
    }
    @Test
    public void run() throws Exception{

        model.run( 232323L );

        Thread.sleep( 100000000 );
    }

    /**
     * 检测周线上摆的情况
     */
    private void checkWeekUp(){

    }

}