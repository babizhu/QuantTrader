package org.bbz.stock.quanttrader;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.bbz.stock.quanttrader.core.QuantTradeContext;
import org.bbz.stock.quanttrader.model.impl.simpletrade.SimpleTradeModel;

/**
 * Created by liu_k on 2017/6/20.
 * 启动
 */
public class LauncherVerticle extends AbstractVerticle{

    public static final String TRADE_FEE = "0.0004";
    private static final int PORT = 8000;

    @Override
    public void start(  ) {
        QuantTradeContext quantTradeContext = new QuantTradeContext( TRADE_FEE, "100000" );
        SimpleTradeModel simpleTradeModel = new SimpleTradeModel( quantTradeContext );
        vertx.setPeriodic( 1000,simpleTradeModel::run );
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
