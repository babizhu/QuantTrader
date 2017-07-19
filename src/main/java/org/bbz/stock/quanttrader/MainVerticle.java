package org.bbz.stock.quanttrader;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.bbz.stock.quanttrader.database.DatabaseVercitle;
import org.bbz.stock.quanttrader.http.HttpServerVerticle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by liu_k on 2017/6/20.
 * 启动类，细节有待斟酌
 */
@Slf4j
public class MainVerticle extends AbstractVerticle{

    private static final int PORT = 8000;

//    private HttpClient httpClient;


    @Override
    public void start( Future<Void> startFuture ){

        Future<String> dbVerticleDeployment = Future.future();
        DeploymentOptions dbOptions = new DeploymentOptions().setConfig( config().getJsonObject( "db" ) );
        vertx.deployVerticle( new DatabaseVercitle(), dbOptions, dbVerticleDeployment.completer() );
        dbVerticleDeployment.compose( id -> {
            Future<String> httpVerticleDeployment = Future.future();

            DeploymentOptions options = new DeploymentOptions().setConfig( config().getJsonObject( "server" ) );
            vertx.deployVerticle(
//                    "com.srxk.car.user.behavioranalysis.http.HttpServerVerticle",
                    new HttpServerVerticle(),
                    options,
                    httpVerticleDeployment.completer() );
            return httpVerticleDeployment;

        } ).compose( id -> {
            Future<String> tradeVerticleDeployment = Future.future();
            DeploymentOptions options = new DeploymentOptions().setInstances( 1 ).setConfig( config().getJsonObject( "server" ) );
            vertx.deployVerticle(
                    "org.bbz.stock.quanttrader.trade.TradeVerticle",
//                    new HttpServerVerticle(),
                    options,
                    tradeVerticleDeployment.completer() );
            return tradeVerticleDeployment;
        } ).setHandler( ar -> {
            if( ar.succeeded() ) {
                startFuture.complete();
            } else {
                startFuture.fail( ar.cause() );
            }
        } );
    }

    public static void main( String[] args ) throws IOException{
        final VertxOptions vertxOptions = new VertxOptions();
        vertxOptions.setBlockedThreadCheckInterval( 1000000 );
        Vertx vertx = Vertx.vertx( vertxOptions );

        DeploymentOptions options = new DeploymentOptions();
        options.setInstances( 1 );

        String content = new String( Files.readAllBytes( Paths.get( "resources/application-conf.json" ) ) );
        final JsonObject config = new JsonObject( content );

        log.info( config.toString() );
        options.setConfig( config );

        vertx.deployVerticle( MainVerticle.class.getName(), options, res -> {
            if( res.succeeded() ) {
                log.info( " server started " );
            } else {
                res.cause().printStackTrace();
            }
        } );

    }
//        final VertxOptions vertxOptions = new VertxOptions();
//        vertxOptions.setBlockedThreadCheckInterval( 1000000 );
//        Vertx vertx = Vertx.vertx( vertxOptions );
//
//        DeploymentOptions options = new DeploymentOptions();
////        options.setInstances( 1 );
//
//        vertx.deployVerticle( MainVerticle.class.getName(), options, res -> {
//            if( res.succeeded() ) {
//                System.out.println( "web server started at port " + PORT + ", please click http://localhost:" + PORT + " to visit!" );
//            } else {
//                res.cause().printStackTrace();
//            }
//        } );
//    }
}
