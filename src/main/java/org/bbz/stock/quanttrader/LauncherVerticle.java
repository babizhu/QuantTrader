package org.bbz.stock.quanttrader;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonArray;
import org.bbz.stock.quanttrader.core.OrderCost;
import org.bbz.stock.quanttrader.core.QuantTradeContext;
import org.bbz.stock.quanttrader.model.impl.simpletrade.SimpleTradeModel;

/**
 * Created by liu_k on 2017/6/20.
 * 启动
 */
public class LauncherVerticle extends AbstractVerticle{

    private static final String TRADE_FEE = "0.0004";
    private static final int PORT = 8000;

    private HttpClient httpClient;

    @Override
    public void start(){
        QuantTradeContext quantTradeContext = new QuantTradeContext( new OrderCost(), "100000" );

        String stockId = "600109";
        SimpleTradeModel simpleTradeModel = new SimpleTradeModel( quantTradeContext, stockId );
//        vertx.setPeriodic( 1000, simpleTradeModel::run );
        httpClient = vertx.createHttpClient();

        httpClient.getNow( 8888, "localhost", "/", resp -> resp.bodyHandler( body->{
//            BigDecimal prices =
            JsonArray objects = body.toJsonArray();
            for( Object object : objects ) {
                System.out.println( object );
            }
        } ) );

    }

    public static void main( String[] args ){
        final VertxOptions vertxOptions = new VertxOptions();
        vertxOptions.setBlockedThreadCheckInterval( 1000000 );
        Vertx vertx = Vertx.vertx( vertxOptions );

        DeploymentOptions options = new DeploymentOptions();
//        options.setInstances( 1 );

        vertx.deployVerticle( LauncherVerticle.class.getName(), options, res -> {
            if( res.succeeded() ) {
                System.out.println( "web server started at port " + PORT + ", please click http://localhost:" + PORT + " to visit!" );
            }
        } );

    }
}
