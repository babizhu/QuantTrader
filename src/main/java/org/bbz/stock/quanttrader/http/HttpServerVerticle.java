package org.bbz.stock.quanttrader.http;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTOptions;
import io.vertx.ext.auth.mongo.MongoAuth;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import lombok.extern.slf4j.Slf4j;
import org.bbz.stock.quanttrader.http.handler.trade.TradeHandler;
import org.bbz.stock.quanttrader.http.handler.trade.UserHandler;

/**
 * Created by liulaoye on 17-7-11.
 * web server verticle
 */
@Slf4j
public class HttpServerVerticle extends AbstractVerticle{

    private static final String API_PREFIX = "/api/";
    JWTAuth jwtAuth;

    @Override
    public void start( Future<Void> startFuture ) throws Exception{
        MongoClient client = MongoClient.createShared(vertx, mongoClientConfig);
        JsonObject authProperties = new JsonObject();
        MongoAuth authProvider = MongoAuth.create(client, authProperties);
        authProvider.authenticate(  );


        HttpServer server = vertx.createHttpServer();
        Router router = Router.router( vertx );
        initBaseHandler( router );

        EventBus eventBus = vertx.eventBus();
        dispatcher( router, eventBus );
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


    private void initBaseHandler( Router router ){
        router.route().handler( CookieHandler.create() );
        router.route().handler( SessionHandler.create( LocalSessionStore.create( vertx ) ) );
        router.route().handler( BodyHandler.create() );


        JsonObject config = new JsonObject().put( "keyStore", new JsonObject()
                .put( "path", "/home/liulaoye/jwt/keystore.jceks" )
                .put( "type", "jceks" )
                .put( "password", "secret" ) );

        jwtAuth = JWTAuth.create( vertx, config );
        router.route( "/s/*" ).handler( JWTAuthHandler.create( jwtAuth, "/s/newToken" ) );
        router.route( "/s/newToken" ).handler( this::login );
        router.route( "/s/isLogin" ).handler( this::isLogin );
    }

    private void isLogin( RoutingContext ctx ){
        ctx.response().end( ctx.user().principal().toString() );
    }

    private void login( RoutingContext ctx ){
        String token = jwtAuth.generateToken( new JsonObject().put( "sub", "liulaoye" ), new JWTOptions() );
        ctx.response().putHeader( "Authorization","Bearer "+ token ).end( "success" );

    }

    private void dispatcher( Router mainRouter, EventBus eventBus ){
        Router restAPI = Router.router( vertx );
        mainRouter.mountSubRouter( API_PREFIX + "trade", new TradeHandler( eventBus ).addRouter( restAPI ) );
        mainRouter.mountSubRouter( API_PREFIX + "user", new UserHandler( eventBus ).addRouter( restAPI ) );
    }
//
//    private void TradeLastRunInfo( RoutingContext ctx ){
//        DeliveryOptions options = new DeliveryOptions().addHeader( "action", EventBusCommand.TRADE_GET_INFO.name() );
//        final int taskId = Integer.parseInt( ctx.request().getParam( "taskId" ) );
//        final JsonObject arguments = new JsonObject().put( "taskId", taskId );
//        eventBus.send( EventBusAddress.TRADE_MODEL_ADDR + "0", arguments, options, reply -> {
//            if( reply.succeeded() ) {
//                final JsonObject body = (JsonObject) reply.result().body();
////                String res = "<http><head></head><meta http-equiv=\"content-type\" content=\"text/html;charset=utf-8\"><meta http-equiv=\"refresh\" content=\"10\"><body>";
//                String res = body.getString( "res" ) + "<br/><br/><br/><br/><br/><br/><br/>";
//                res += "<h3>L-L(ver 1.0)必发财炒股鸡</h3>";
////                res += "</body></html>";
//
////                ctx.response().putHeader( "charset","UTF-8" );
//                ctx.response().setStatusCode( 200 )
//                        .putHeader( "content-type", "text/html; charset=utf-8" ).end( res );
//                log.info( res );
//            } else {
////                ctx.fail( reply.cause() );
//                ReplyException e = (ReplyException) reply.cause();
////                final Throwable cause = e.getCause();
////                cause.getMessage();
////                e.getCause().getMessage();
////                ctx.response().setStatusCode( 500 ).end( e.failureCode() + "" );
//                ctx.fail( reply.cause() );
//            }
//        } );
//    }
//
//    private void TradeRun( RoutingContext ctx ){
//        final JsonObject argument = new JsonObject().put( JsonConsts.CTX_KEY, new JsonObject().put( JsonConsts.INIT_BALANCE_KEY, "100000" ) );
//        argument.put( JsonConsts.MODEL_CLASS_KEY, "WaveTradeModel" );
//        argument.put( "taskId", Integer.parseInt( ctx.request().getParam( "taskId" ) ) );
//        System.out.println( argument );
//
//        DeliveryOptions options = new DeliveryOptions().addHeader( "action", EventBusCommand.TRADE_START.name() );
//
//        eventBus.send( EventBusAddress.TRADE_MODEL_ADDR + "0", argument, options, reply -> {
//            if( reply.succeeded() ) {
//                ctx.response().setStatusCode( 200 ).end( "ok" );
//                log.info( " ok" );
//            } else {
////                ctx.fail( reply.cause() );
//                ctx.response().setStatusCode( 500 ).end( reply.cause().getMessage() );
//            }
//        } );
//    }
//
//    /**
//     * @param ctx
//     */
//    private void Info( RoutingContext ctx ){
////        final int id = Integer.parseInt( ctx.request().getParam( "id" ) );
//        for( int i = 0; i < 20; i++ ) {
//
//            String address = EventBusAddress.TRADE_MODEL_ADDR + (i % 10);
//            eventBus.send( address, address );
//        }
//
//    }
//
//    private void responseError( RoutingContext context, String errorDesc ){
//        context.response().setStatusCode( 500 ).end( errorDesc );
//
//    }
}


