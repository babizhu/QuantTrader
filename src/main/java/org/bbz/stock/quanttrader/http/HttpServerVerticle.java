package org.bbz.stock.quanttrader.http;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;
import lombok.extern.slf4j.Slf4j;
import org.bbz.stock.quanttrader.consts.ErrorCode;
import org.bbz.stock.quanttrader.consts.ErrorCodeException;
import org.bbz.stock.quanttrader.http.handler.trade.TradeHandler;
import org.bbz.stock.quanttrader.http.handler.tradingstrategy.TradingStrategyHandler;
import org.bbz.stock.quanttrader.http.handler.user.AuthHandler;
import org.bbz.stock.quanttrader.http.handler.user.LoginHandler;
import org.bbz.stock.quanttrader.http.handler.user.UserHandler;

/**
 * Created by liulaoye on 17-7-11. web server verticle
 */
@Slf4j
public class HttpServerVerticle extends AbstractVerticle {

  private static final String API_PREFIX = "/api/";
  private JWTAuth jwtAuthProvider;
  private EventBus eventBus;

  @Override
  public void start(Future<Void> startFuture) throws Exception {

    HttpServer server = vertx.createHttpServer();
    Router router = Router.router(vertx);
    eventBus = vertx.eventBus();
    initHandler(router);

    int portNumber = config().getInteger("port", 8080);
    server
        .requestHandler(router::accept)
        .listen(portNumber, ar -> {
          if (ar.succeeded()) {
            log.info("HTTP server running on port " + portNumber);
            startFuture.complete();
          } else {
            log.error("Could not start a HTTP server", ar.cause());
            startFuture.fail(ar.cause());
          }
        });
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
//        authProvider = MongoAuth.MapperFromDB( mongoClient, authProperties );
//    }


  private void initHandler(Router router) {
//        router.route().handler( CookieHandler.MapperFromDB());
//        router.route().handler( SessionHandler.MapperFromDB( LocalSessionStore.MapperFromDB(vertx)));
//        router.route().handler(UserSessionHandler.MapperFromDB(auth));  (1)
    router.route().handler(BodyHandler.create());
    JsonObject jwtConfig = new JsonObject().put("permissionsClaimKey", "roles")
        .put("keyStore", new JsonObject()
            .put("path", "./resources/keystore.jceks")
            .put("type", "jceks")
            .put("password", "secret"));

    jwtAuthProvider = JWTAuth.create(vertx, jwtConfig);

//        router.route( API_PREFIX+"*" ).handler( new CustomJWTAuthHandlerImpl(eventBus, jwtAuthProvider ) );//暂时只能屏蔽
    dispatcher(router);

    router.route().handler(StaticHandler.create());//必须放在最后

//        router.route( "/login" ).handler( this::login );
//        router.route( "/s/isLogin" ).handler( this::isLogin );
//        router.route( "/createUser" ).handler( this::createUser );
  }

  private void dispatcher(Router mainRouter) {
//        mainRouter.route().handler(CorsHandler.MapperFromDB("vertx\\.io").allowedMethod(HttpMethod.));

    final CorsHandler corsHandler = CorsHandler.create("*")
        .maxAgeSeconds(17280000)
        .allowedMethod(HttpMethod.GET)
        .allowedMethod(HttpMethod.POST)
        .allowedHeader("Content-Type");

    mainRouter.route(API_PREFIX + "*").handler(corsHandler)
        .failureHandler(ctx -> {
          if( ctx.statusCode() == 404){
            JsonObject errorResponse = new JsonObject()
                .put("eid", 404)
                .put("msg", "Not Found");
            ctx.response().setStatusCode(500).end(errorResponse.toString());
            return;
          }
          int errId = ErrorCode.SYSTEM_ERROR.toNum();
//      if (ctx.failure() instanceof ReplyException) {
//        errId = ((ReplyException) ctx.failure()).failureCode();
//      }//以上代码不太确定是否有必要存在，
          if (ctx.failure() instanceof ErrorCodeException) {
            errId = ((ErrorCodeException) ctx.failure()).getErrorCode();

          }
          JsonObject errorResponse = new JsonObject()
              .put("eid", errId)
              .put("msg", ctx.failure().getMessage());
          ctx.response().setStatusCode(500).end(errorResponse.toString());
        });
    mainRouter.mountSubRouter(API_PREFIX,
        new LoginHandler(eventBus, jwtAuthProvider).addRouter(Router.router(vertx)));

    mainRouter.mountSubRouter(API_PREFIX + "trade",
        new TradeHandler(eventBus).addRouter(Router.router(vertx)));
    mainRouter.mountSubRouter(API_PREFIX + "user",
        new UserHandler(eventBus).addRouter(Router.router(vertx)));
    mainRouter.mountSubRouter(API_PREFIX + "auth",
        new AuthHandler(eventBus).addRouter(Router.router(vertx)));
    mainRouter.mountSubRouter(API_PREFIX + "tradingstrategy",
        new TradingStrategyHandler(eventBus).addRouter(Router.router(vertx)));
//        mainRouter.route(API_PREFIX+"*").handler( new ResponseHandler() );

  }
}