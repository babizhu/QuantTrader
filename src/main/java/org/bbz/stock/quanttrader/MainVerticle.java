package org.bbz.stock.quanttrader;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.redis.RedisClient;
import org.bbz.stock.quanttrader.core.OrderCost;
import org.bbz.stock.quanttrader.core.QuantTradeContext;
import org.bbz.stock.quanttrader.model.impl.wavetrade.WaveTrideModel;
import org.bbz.stock.quanttrader.stockdata.RedisDataProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by liu_k on 2017/6/20.
 * 启动
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
        stockMap.put( "000902", 0 );
        stockMap.put( "300336", 0 );
        stockMap.put( "600592", 0 );
        stockMap.put( "000789", 0 );
        stockMap.put( "600758", 0 );

        ctx.getPortfolio().setStocks( stockMap );

        final WaveTrideModel model = new WaveTrideModel( ctx, RedisDataProvider.INSTANCE() );

        vertx.setPeriodic( 30000, model::run );

    }

    public static void main( String[] args ){
        final VertxOptions vertxOptions = new VertxOptions();
        vertxOptions.setBlockedThreadCheckInterval( 1000000 );
        Vertx vertx = Vertx.vertx( vertxOptions );

        DeploymentOptions options = new DeploymentOptions();
//        options.setInstances( 1 );

        vertx.deployVerticle( MainVerticle.class.getName(), options, res -> {
            if( res.succeeded() ) {
                System.out.println( "web server started at port " + PORT + ", please click http://localhost:" + PORT + " to visit!" );
            }
        } );

    }
}
