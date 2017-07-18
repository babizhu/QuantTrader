package org.bbz.stock.quanttrader.trade.stockdata.impl;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.redis.RedisClient;
import org.bbz.stock.quanttrader.consts.KLineType;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;

/**
 * Created by liulaoye on 17-7-17.
 * TuShareDataProviderTest
 */
public class TuShareDataProviderTest{
    private TuShareDataProvider provider;
    @Before
    public void init(){
        final Vertx vertx = Vertx.vertx();
        final RedisClient redisClient = RedisClient.create( vertx );
        final HttpClientOptions httpClientOptions = new HttpClientOptions();
        httpClientOptions.setDefaultPort( 8888 ).setDefaultHost( "localhost" ).setConnectTimeout( 4000 ).setKeepAlive( true );
        provider = TuShareDataProvider.createShare( redisClient, vertx.createHttpClient( httpClientOptions ) );

    }

    @Test
    public void getSimpleKBar() throws Exception{
    }



    @Test
    public void getSimpleKBarEx() throws Exception{


        provider.getSimpleKBarEx( "6008438", KLineType.DAY,100,
                LocalDate.parse( "2017-07-14" ),
                LocalDate.parse( "2017-07-17" ),null );
        Thread.sleep( 10000000 );
    }

    @Test
    public void getCurrentKbar() throws Exception{
        provider.getCurrentKbar( "600848",res->{
            System.out.println(res);
        } );
        Thread.sleep( 10000000 );

    }

}