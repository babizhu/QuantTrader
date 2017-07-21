package org.bbz.stock.quanttrader.http;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.extern.slf4j.Slf4j;
import org.bbz.stock.quanttrader.consts.Command;
import org.bbz.stock.quanttrader.consts.EventBusAddress;
import org.bbz.stock.quanttrader.consts.JsonConsts;

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
        router.route( "/trade/run" ).handler( this::TradeRun );
        router.route( "/trade/info" ).handler( this::TradeLastRunInfo );
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

    private void TradeLastRunInfo( RoutingContext ctx ){
        DeliveryOptions options = new DeliveryOptions().addHeader( "action", Command.TRADE_LAST_RUN_INFO.name() );
        final int taskId = Integer.parseInt( ctx.request().getParam( "taskId" ) );
        final JsonObject arguments = new JsonObject().put( "taskId", taskId );
        eventBus.send( EventBusAddress.TRADE_MODEL_ADDR + "0", arguments, options, reply -> {
            if( reply.succeeded() ) {
                final JsonObject body = (JsonObject) reply.result().body();
//                String res = "<http><head></head><meta http-equiv=\"content-type\" content=\"text/html;charset=utf-8\"><meta http-equiv=\"refresh\" content=\"10\"><body>";
                String res = body.getString( "res" )+"<br/><br/><br/><br/><br/><br/><br/>";
                res += "<h3>L-L(ver 1.0)必发财炒股鸡</h3>";
//                res += "</body></html>";

//                ctx.response().putHeader( "charset","UTF-8" );
                ctx.response().setStatusCode( 200 )
                        .putHeader("content-type","text/html; charset=utf-8").end( res );
                log.info( res );
            } else {
//                ctx.fail( reply.cause() );
                ReplyException e = (ReplyException) reply.cause();
//                final Throwable cause = e.getCause();
//                cause.getMessage();
//                e.getCause().getMessage();
//                ctx.response().setStatusCode( 500 ).end( e.failureCode() + "" );
                ctx.fail(reply.cause());
            }
        } );
    }

    private void TradeRun( RoutingContext ctx ){
        final JsonObject argument = new JsonObject().put( JsonConsts.CTX_KEY, new JsonObject().put( JsonConsts.INIT_BALANCE_KEY, "100000" ) );
        argument.put( JsonConsts.MODEL_CLASS_KEY, "WaveTradeModel" );
        argument.put( "taskId", Integer.parseInt( ctx.request().getParam( "taskId" ) ) );
        System.out.println( argument );

        DeliveryOptions options = new DeliveryOptions().addHeader( "action", Command.TRADE_RUN.name() );

        eventBus.send( EventBusAddress.TRADE_MODEL_ADDR + "0", argument, options, reply -> {
            if( reply.succeeded() ) {
                ctx.response().setStatusCode( 200 ).end( "ok" );
                log.info( " ok" );
            } else {
//                ctx.fail( reply.cause() );
                ctx.response().setStatusCode( 500 ).end( reply.cause().getMessage() );
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


