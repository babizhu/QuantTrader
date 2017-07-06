package org.bbz.stock.quanttrader;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;
import org.bbz.stock.quanttrader.core.OrderCost;
import org.bbz.stock.quanttrader.core.QuantTradeContext;
import org.bbz.stock.quanttrader.model.impl.simpletrade.SimpleTradeModel;
import org.bbz.stock.quanttrader.stockdata.RedisDataProvider;

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
        QuantTradeContext quantTradeContext = new QuantTradeContext( new OrderCost(), "100000" );

        String stockId = "600109";
        SimpleTradeModel simpleTradeModel = new SimpleTradeModel( quantTradeContext, stockId );
//        vertx.setPeriodic( 1000, simpleTradeModel::run );
        httpClient = vertx.createHttpClient( new HttpClientOptions().setMaxPoolSize( 1 ) );

        httpClient.getNow( 8888, "localhost", "/", resp -> resp.bodyHandler( body -> {
//            BigDecimal prices =
            JsonArray objects = body.toJsonArray();
            for( Object object : objects ) {
                System.out.println( object );
            }
        } ) );

        RedisOptions config = new RedisOptions()
                .setHost( "127.0.0.1" );

        RedisClient redis = RedisClient.create( vertx, config );
//        redisDataProvider = new RedisDataProvider( redis,httpClient );
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
