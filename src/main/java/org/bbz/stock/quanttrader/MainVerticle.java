package org.bbz.stock.quanttrader;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.bbz.stock.quanttrader.database.MongoDatabaseVercitle;
import org.bbz.stock.quanttrader.http.HttpServerVerticle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by liu_k on 2017/6/20.
 * 启动类，细节有待斟酌
 *
 * db.user.insert({'username':'admin','salt':'C428A0BACCFC909EE7F3FB88CE77DED2FDFC103975BAADBC75EDB32F09C39AF5','email':'185938@qq.com','phone':'18698787878','password':'3102D2665788795D2590557669C37924E3378DCAC1B49E8D98F80C817DC98418772EEFAE6B2CB7E4886EB08EA06C5E1020952A7029D72478EA551947F6A02BDA'});
 * db.tradingstrategy.insert({'name':'卢哥大法','desc':'通过波浪理论赚大钱','modelClass':'WaveTradeModel','owner':'sys'})
 * db.employee.update({username:'jim'},{$set:{age:22}},false,true);
 *  db.trade.update({_id:"59c875a6d5c0783924120ca3", "tradeRecords.id":"50312afd-8313-4c08-a129-2f8f5dd1ffe6"}, { $set : {"tradeRecords.$.share":2400,"tradeRecords.$.price":1.2121 }}
 */
@Slf4j
public class MainVerticle extends AbstractVerticle{

//    private static final int PORT = 8000;

//    private HttpClient httpClient;


    @Override
    public void start( Future<Void> startFuture ){

        Future<String> dbVerticleDeployment = Future.future();
        DeploymentOptions dbOptions = new DeploymentOptions().setConfig( config().getJsonObject( "mongo" ) );
        vertx.deployVerticle( new MongoDatabaseVercitle(), dbOptions, dbVerticleDeployment.completer() );
        dbVerticleDeployment.compose( id -> {
            Future<String> httpVerticleDeployment = Future.future();
            DeploymentOptions options = new DeploymentOptions().setConfig(
                    config().getJsonObject( "server" ).
                        put( "mongo",config().getJsonObject( "mongo" ) ) );

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

        System.out.println( "vertx.logger-delegate-factory-class-name=" + System.getProperty( "vertx.logger-delegate-factory-class-name" ) );
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
