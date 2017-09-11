package org.bbz.stock.quanttrader.database;


import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import lombok.extern.slf4j.Slf4j;
import org.bbz.stock.quanttrader.consts.ErrorCode;
import org.bbz.stock.quanttrader.consts.EventBusAddress;
import org.bbz.stock.quanttrader.consts.EventBusCommand;
import org.bbz.stock.quanttrader.database.service.AuthService;
import org.bbz.stock.quanttrader.database.service.TradeService;
import org.bbz.stock.quanttrader.database.service.UserService;

/**
 * Created by liulaoye on 17-7-10.
 * mongo db 数据库 vercitle
 */
@Slf4j
public class MongoDatabaseVercitle extends AbstractVerticle{
    private UserService userService;
    private TradeService tradeService;
    private AuthService authService;

    @Override
    public void start( Future<Void> startFuture ) throws Exception{
        JsonObject config = config();
        String uri = config.getString( "uri", "mongodb://localhost:27017" );
        String db = config.getString( "db" );

        JsonObject mongoconfig = new JsonObject()
                .put( "connection_string", uri )
                .put("connectTimeoutMS",5000)
                .put("serverSelectionTimeoutMS",5000)
                .put( "db_name", db );

        MongoClient mongoClient = MongoClient.createShared( vertx, mongoconfig );
        userService = new UserService( mongoClient );
        authService = new AuthService( mongoClient );
        tradeService = new TradeService( mongoClient );
        vertx.eventBus().consumer( EventBusAddress.DB_ADDR, this::onMessage );
        startFuture.complete();
    }

    private void onMessage( Message<JsonObject> message ){
        if( !message.headers().contains( "action" ) ) {
            message.fail( ErrorCode.NOT_IMPLENMENT.toNum(), "No action header specified" );
        }
        String action = message.headers().get( "action" );
        try {
            switch( EventBusCommand.valueOf( action ) ) {
                case DB_USER_INSERT:
                    userService.save( message );
                    break;
                case DB_USER_UPDATE:
                    userService.update(message);
                    break;
                case DB_USER_DELETE:
                    userService.delete(message);
                    break;
                case DB_USER_QUERY:
                    userService.query( message );
                    break;
                case DB_ROLE_SAVE:
                    authService.save( message );
                    break;
                case DB_ROLE_QUERY:
                    authService.query( message );
                    break;
                default:
                    message.fail( ErrorCode.BAD_ACTION.toNum(), "Bad action: " + action );
            }
        } catch( Exception e ) {
            message.fail( ErrorCode.SYSTEM_ERROR.toNum(), e.toString() );
            e.printStackTrace();
        }
    }
//
//
//    private void reportError( Message<JsonObject> message, Throwable cause ){
//        log.error( "Database query error", cause );
//        message.fail( ErrorCode.DB_ERROR.ordinal(), cause.getMessage() );
//    }
}
