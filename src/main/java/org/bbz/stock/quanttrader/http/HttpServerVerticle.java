package org.bbz.stock.quanttrader.http;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.extern.slf4j.Slf4j;
import org.bbz.stock.quanttrader.consts.EventBusAddress;

/**
 * Created by liulaoye on 17-7-11.
 * web server verticle
 */
@Slf4j
public class HttpServerVerticle extends AbstractVerticle{

    private EventBus eventBus = null;

    @Override
    public void start( Future<Void> startFuture ) throws Exception{
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router( vertx );
        router.route().handler( BodyHandler.create() );
        router.route( "/info" ).handler( this::Info );
        int portNumber = config().getInteger( "port", 8080 );
        eventBus = vertx.eventBus();
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
     * @param ctx
     */
    private void Info( RoutingContext ctx ){
//        final int id = Integer.parseInt( ctx.request().getParam( "id" ) );
        for( int i = 0; i < 20; i++ ) {

            String address = EventBusAddress.TRADE_MODEL_ADDR + (i % 10);
            eventBus.send( address, address );
        }

    }

    private void responseError( RoutingContext context, String errorDesc ){
        context.response().setStatusCode( 500 ).end( errorDesc );

    }
}


