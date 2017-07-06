package org.bbz.stock.quanttrader.model.impl.wavetrade;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.redis.RedisClient;
import org.bbz.stock.quanttrader.core.OrderCost;
import org.bbz.stock.quanttrader.core.QuantTradeContext;
import org.bbz.stock.quanttrader.stockdata.RedisDataProvider;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liulaoye on 17-7-6.
 */
public class WaveTrideModelTest{
    @Test
    public void run() throws Exception{
        final Vertx vertx = Vertx.vertx();
        final RedisClient redisClient = RedisClient.create( vertx );
        final HttpClientOptions httpClientOptions = new HttpClientOptions();
        httpClientOptions.setDefaultPort( 8888 ).setDefaultHost( "localhost" ).setConnectTimeout( 4000 )
                .setKeepAlive( true );
        RedisDataProvider.create( redisClient, vertx.createHttpClient( httpClientOptions ) );
        final WaveTrideModel model = new WaveTrideModel( new QuantTradeContext( new OrderCost(), "10" ), RedisDataProvider.INSTANCE() );
        model.run( 232323l );

        Thread.sleep( 100000 );
    }

    @Test
    public void xx(){
        List<Integer> l = new ArrayList<>(  );
        for( int i = 0; i < 4; i++ ) {
            l.add( i );
        }

        System.out.println( l.subList( l.size() -2, l.size() ));
    }

}