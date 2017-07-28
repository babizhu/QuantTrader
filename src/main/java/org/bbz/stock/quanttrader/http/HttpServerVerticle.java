package org.bbz.stock.quanttrader.http;

import com.google.common.base.Strings;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTOptions;
import io.vertx.ext.auth.mongo.MongoAuth;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.extern.slf4j.Slf4j;
import org.bbz.stock.quanttrader.http.handler.trade.TradeHandler;
import org.bbz.stock.quanttrader.http.handler.user.AuthHandler;
import org.bbz.stock.quanttrader.http.handler.user.UserHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by liulaoye on 17-7-11.
 * web server verticle
 */
@Slf4j
public class HttpServerVerticle extends AbstractVerticle{

    private static final String API_PREFIX = "/api/";
    private MongoAuth authProvider;
    private JWTAuth jwtAuthProvider;
    private AtomicInteger ids = new AtomicInteger( 0 );

    @Override
    public void start( Future<Void> startFuture ) throws Exception{

        initAuthProvider( startFuture );
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

    private void initAuthProvider( Future<Void> startFuture ){
        JsonObject config = config().getJsonObject( "mongo" );
        String uri = config.getString( "uri", "mongodb://localhost:27017" );
        String db = config.getString( "db" );
        if( db == null ) {
            startFuture.fail( "没有指定db" );
        }
        JsonObject mongoconfig = new JsonObject()
                .put( "connection_string", uri )
                .put( "db_name", db );

        MongoClient mongoClient = MongoClient.createShared( vertx, mongoconfig );
        JsonObject authProperties = new JsonObject();
        authProvider = MongoAuth.create( mongoClient, authProperties );
    }


    private void initBaseHandler( Router router ){
//        router.route().handler( CookieHandler.create() );
//        router.route().handler( SessionHandler.create( LocalSessionStore.create( vertx ) ) );
        router.route().handler( BodyHandler.create() );


        JsonObject config = new JsonObject().put( "keyStore", new JsonObject()
                .put( "path", "./resources/keystore.jceks" )
                .put( "type", "jceks" )
                .put( "password", "secret" ) );

        jwtAuthProvider = JWTAuth.create( vertx, config );
//        router.route( "/api/*" ).handler( new CustomJWTAuthHandlerImpl( jwtAuthProvider ) );
        router.route( "/login" ).handler( this::login );
        router.route( "/s/isLogin" ).handler( this::isLogin );
        router.route( "/createUser" ).handler( this::createUser );
    }

    private void isLogin( RoutingContext ctx ){
        ctx.response().end( ctx.user().principal().toString() );
    }

    private void createUser( RoutingContext ctx ){
        final HttpServerRequest request = ctx.request();
        String name = request.getParam( "name" );
        if( Strings.isNullOrEmpty(name)){
            name = "liukun"+ids.getAndAdd( 1 );
        }
        String password = request.getParam( "password" );
        if( Strings.isNullOrEmpty(password)){
            password = "123456";
        }

        List<String> roles = new ArrayList<>();
        roles.add( "admin" );
        roles.add( "sys" );
        List<String> permissions = new ArrayList<>();
        permissions.add( "/sys/user/save" );
        permissions.add( "/sys/user/del" );
        String finalName = name;
        String finalPassword = password;
        authProvider.insertUser( name,password,roles,permissions, res->{
            if(res.succeeded()){
                ctx.response().end( "添加用户成功！name=" + finalName +",password=" + finalPassword );
            }else {
                ctx.response().end( res.cause().getMessage() );
            }
        } );
    }
    private void login( RoutingContext ctx ){

        ctx.reroute("/api/user/login?" + ctx.request().query());
//
//        JsonObject authInfo = new JsonObject()
//                .put( "username", "liukun1" )
//                .put( "password", "123456" );
//        authProvider.authenticate( authInfo, res -> {
//            if( res.succeeded() ) {
//                User user = res.result();
//                ctx.response().end( user.principal().toString());
//            } else {
//                ctx.response().end( res.cause().getMessage() );
//                return;
//            }
//        } );
//        String token = jwtAuthProvider.generateToken( // <3>
//                new JsonObject()
//                        .put( "username", "liulaoye" )
//                        .put("canCreate", true)
//                        .put( "permissions", new JsonArray(  ).add( "admin" ).add( "/sys/user/save" )),
//                new JWTOptions()
//                        .setSubject( "Wiki API" )
//                        .setIssuer( "Vert.x" ) );

//ctx.response().end( token );
//        String token = jwtAuth.generateToken( new JsonObject().put( "sub", "liulaoye" ), new JWTOptions() );
//        ctx.response().putHeader( "Authorization", "Bearer " + token ).end( "success" );

    }

    private void dispatcher( Router mainRouter, EventBus eventBus ){
//        Router restAPI = Router.router( vertx );
        mainRouter.mountSubRouter( API_PREFIX + "trade", new TradeHandler( eventBus ).addRouter( Router.router( vertx ) ) );
        mainRouter.mountSubRouter( API_PREFIX + "user", new UserHandler( eventBus,jwtAuthProvider ).addRouter( Router.router( vertx ) ) );
        mainRouter.mountSubRouter( API_PREFIX + "auth", new AuthHandler( eventBus ).addRouter( Router.router( vertx ) ) );
    }
}