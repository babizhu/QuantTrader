package org.bbz.stock.quanttrader.http;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.extern.slf4j.Slf4j;
import org.bbz.stock.quanttrader.http.handler.auth.CustomJWTAuthHandlerImpl;
import org.bbz.stock.quanttrader.http.handler.trade.TradeHandler;
import org.bbz.stock.quanttrader.http.handler.user.AuthHandler;
import org.bbz.stock.quanttrader.http.handler.user.LoginHandler;
import org.bbz.stock.quanttrader.http.handler.user.UserHandler;

/**
 * Created by liulaoye on 17-7-11.
 * web server verticle
 */
@Slf4j
public class HttpServerVerticle extends AbstractVerticle{

    private static final String API_PREFIX = "/api/";
    private JWTAuth jwtAuthProvider;
private  EventBus eventBus;
    @Override
    public void start( Future<Void> startFuture ) throws Exception{

        HttpServer server = vertx.createHttpServer();
        Router router = Router.router( vertx );
        eventBus = vertx.eventBus();
        initBaseHandler( router );

        dispatcher( router );
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

//    private void initAuthProvider( Future<Void> startFuture ){
//        JsonObject config = config().getJsonObject( "mongo" );
//        String uri = config.getString( "uri", "mongodb://localhost:27017" );
//        String db = config.getString( "db" );
//        if( db == null ) {
//            startFuture.fail( "没有指定db" );
//        }
//        JsonObject mongoconfig = new JsonObject()
//                .put( "connection_string", uri )
//                .put( "db_name", db );
//
//        MongoClient mongoClient = MongoClient.createShared( vertx, mongoconfig );
//        JsonObject authProperties = new JsonObject();
//        authProvider = MongoAuth.create( mongoClient, authProperties );
//    }


    private void initBaseHandler( Router router ){
//        router.route().handler( CookieHandler.create());
//        router.route().handler(BodyHandler.create());
//        router.route().handler( SessionHandler.create( LocalSessionStore.create(vertx)));
//        router.route().handler(UserSessionHandler.create(auth));  (1)

        router.route().handler( BodyHandler.create() );
        JsonObject config = new JsonObject().put( "permissionsClaimKey","roles" ).put( "keyStore", new JsonObject()
                .put( "path", "./resources/keystore.jceks" )
                .put( "type", "jceks" )

                .put( "password", "secret" ) );

        jwtAuthProvider = JWTAuth.create( vertx, config );

        router.route( "/api/*" ).handler( new CustomJWTAuthHandlerImpl(eventBus, jwtAuthProvider ) );
//        router.route( "/login" ).handler( this::login );
//        router.route( "/s/isLogin" ).handler( this::isLogin );
//        router.route( "/createUser" ).handler( this::createUser );
    }

    private void dispatcher( Router mainRouter){
        mainRouter.mountSubRouter( "/user", new LoginHandler( eventBus, jwtAuthProvider ).addRouter( Router.router( vertx ) ) );

        mainRouter.mountSubRouter( API_PREFIX + "trade", new TradeHandler( eventBus ).addRouter( Router.router( vertx ) ) );
        mainRouter.mountSubRouter( API_PREFIX + "user", new UserHandler( eventBus, jwtAuthProvider ).addRouter( Router.router( vertx ) ) );
        mainRouter.mountSubRouter( API_PREFIX + "auth", new AuthHandler( eventBus ).addRouter( Router.router( vertx ) ) );
    }
}