package org.bbz.stock.quanttrader.http;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.extern.slf4j.Slf4j;
import org.bbz.stock.quanttrader.consts.Consts;

/**
 * Created by liulaoye on 17-7-11.
 * web server verticle
 */
@Slf4j
public class HttpServerVerticle extends AbstractVerticle{
    private static final String dbAddress = Consts.EVENT_BUS_DATABASE_ADDRESS;

    @Override
    public void start( Future<Void> startFuture ) throws Exception{
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router( vertx );
        router.route().handler( BodyHandler.create() );
        router.route( "/addBehavior" ).handler( this::addBehavior );
        int portNumber = config().getInteger( "port", 8080 );
        server
                .requestHandler( router::accept )
                .listen( portNumber, ar -> {
                    if( ar.succeeded() ) {
                        log.info( "HTTP server running on port " + portNumber );
                        startFuture.complete();
                    } else {
                        log.error( "Could not start a HTTP server", ar.cause() );
                        startFuture.fail( ar.cause() );
                    }
                } );
    }

    /**
     *
     * @param context
     */
    private void addBehavior( RoutingContext context ){

        final String user_id = context.request().getParam( "user_id" );
        if( user_id == null || user_id.isEmpty() ) {
            responseError( context, " user_id" );
            return;
        }
        final String behaviors_id = context.request().getParam( "behaviors_id" );
        if( behaviors_id == null || behaviors_id.isEmpty() ) {
            responseError( context, " behaviors_id" );
            return;
        }
        final String terminal_id = context.request().getParam( "terminal_id" );
        if( terminal_id == null || terminal_id.isEmpty() ) {
            responseError( context, " terminal_id" );
            return;
        }
        final String os_version = context.request().getParam( "os_version" );
        if( os_version == null || os_version.isEmpty() ) {
            responseError( context, " os_version" );
            return;
        }
        final String version_id = context.request().getParam( "version_id" );
        if( version_id == null || version_id.isEmpty() ) {
            responseError( context, " version_id" );
            return;
        }

        final String ip = context.request().getParam( "ip" );
        if( ip == null || ip.isEmpty() ) {
            responseError( context, " ip" );
            return;
        }
        final String arguments = context.request().getParam( "arguments" );
        if( arguments == null || arguments.isEmpty() ) {
            responseError( context, " arguments" );
            return;
        }
        JsonObject request = new JsonObject();

        request.put( "user_id", user_id )
                .put( "behaviors_id", behaviors_id )
                .put( "terminal_id", terminal_id )
                .put( "version_id", version_id )
                .put( "os_version", os_version )
                .put( "ip", ip )
                .put( "arguments", arguments );


        DeliveryOptions options = new DeliveryOptions().addHeader( "action", "addBehavior" );
        vertx.eventBus().send( dbAddress, request, options, reply -> {
            if( reply.succeeded() ) {
                context.response().setStatusCode( 200 ).end( "ok" );
                log.info( request + " ok" );
            } else {
//                context.fail( reply.cause() );
                context.response().setStatusCode( 500 ).end( reply.cause().getMessage() );
            }
        } );
    }

    private void responseError( RoutingContext context, String errorDesc ){
        context.response().setStatusCode( 500 ).end( errorDesc );

    }
}


