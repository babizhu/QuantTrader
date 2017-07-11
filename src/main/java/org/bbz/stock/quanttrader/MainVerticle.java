package org.bbz.stock.quanttrader;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.redis.RedisClient;
import org.bbz.stock.quanttrader.trade.core.OrderCost;
import org.bbz.stock.quanttrader.trade.core.QuantTradeContext;
import org.bbz.stock.quanttrader.trade.model.impl.wavetrade.WaveTrideModel;
import org.bbz.stock.quanttrader.trade.stockdata.RedisDataProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by liu_k on 2017/6/20.
 * 启动类，细节有待斟酌
 */
public class MainVerticle extends AbstractVerticle{

    private static final int PORT = 8000;

    private HttpClient httpClient;
    private RedisDataProvider redisDataProvider;


    @Override
    public void start(){

        final RedisClient redisClient = RedisClient.create( vertx );

        final HttpClientOptions httpClientOptions = new HttpClientOptions();
        httpClientOptions.setDefaultPort( 8888 ).setDefaultHost( "localhost" ).setConnectTimeout( 4000 ).setKeepAlive( true );
        RedisDataProvider.create( redisClient, vertx.createHttpClient( httpClientOptions ) );
        QuantTradeContext ctx = new QuantTradeContext( new OrderCost(), "10" );

        Map<String, Integer> stockMap = new HashMap<>();
//        000902，新文化300336，龙溪股份600592，万年青000789，红阳能源600758
//        stockMap.put( "002030", 0 );
//        stockMap.put( "002065", 0 );
//        stockMap.put( "002097", 0 );
//        stockMap.put( "002104", 0 );
//        stockMap.put( "002121", 0 );
//        stockMap.put( "002135", 0 );
//        stockMap.put( "002152", 0 );
//        stockMap.put( "002158", 0 );
//        stockMap.put( "002166", 0 );
//        stockMap.put( "002178", 0 );
//        stockMap.put( "002191", 0 );
//        stockMap.put( "002242", 0 );
//        stockMap.put( "002251", 0 );
//        stockMap.put( "002351", 0 );
//        stockMap.put( "002377", 0 );
//        stockMap.put( "002385", 0 );
//        stockMap.put( "002408", 0 );
//        stockMap.put( "002412", 0 );
//        stockMap.put( "002435", 0 );
//        stockMap.put( "002446", 0 );
//        stockMap.put( "002532", 0 );
//        stockMap.put( "002541", 0 );
//        stockMap.put( "002551", 0 );
//        stockMap.put( "002608", 0 );
//        stockMap.put( "002626", 0 );
//        stockMap.put( "002643", 0 );
        stockMap.put( "002792", 0 );

        ctx.getPortfolio().setStocks( stockMap );

        final WaveTrideModel model = new WaveTrideModel( ctx, RedisDataProvider.INSTANCE() );

//        vertx.setPeriodic( 1000, model::run );
        model.run( 3434343L );

    }

    public static void main( String[] args ){
        System.out.println( "MainVerticle.main" );
        final VertxOptions vertxOptions = new VertxOptions();
        vertxOptions.setBlockedThreadCheckInterval( 1000000 );
        Vertx vertx = Vertx.vertx( vertxOptions );

        DeploymentOptions options = new DeploymentOptions();
//        options.setInstances( 1 );

        vertx.deployVerticle( MainVerticle.class.getName(), options, res -> {
            if( res.succeeded() ) {
                System.out.println( "web server started at port " + PORT + ", please click http://localhost:" + PORT + " to visit!" );
            }else {
                res.cause().printStackTrace();
            }
        } );

    }
}
