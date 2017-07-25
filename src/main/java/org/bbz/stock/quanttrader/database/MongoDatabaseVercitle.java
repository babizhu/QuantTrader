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
import org.bbz.stock.quanttrader.database.service.UserService;

/**
 * Created by liulaoye on 17-7-10.
 * mongo db 数据库 vercitle
 */
@Slf4j
public class MongoDatabaseVercitle extends AbstractVerticle{
    private UserService userService;

    @Override
    public void start( Future<Void> startFuture ) throws Exception{
        JsonObject config = config();
        String uri = config.getString( "uri", "mongodb://localhost:27017" );
        String db = config.getString( "db" );
        if( db == null ) {
            startFuture.fail( "没有指定db" );
        }
        JsonObject mongoconfig = new JsonObject()
                .put( "connection_string", uri )
                .put( "db_name", db );

        MongoClient mongoClient = MongoClient.createShared( vertx, mongoconfig );
        userService = new UserService( mongoClient );
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
                case DB_USER_ADD:
                    userService.addUser( message );

                    break;
                case DB_USER_GET:
                    userService.getAll( message );

                    break;

                default:
                    message.fail( ErrorCode.BAD_ACTION.toNum(), "Bad action: " + action );
            }
        } catch( Exception e ) {
            message.fail( ErrorCode.SYSTEM_ERROR.toNum(), e.toString() );
            e.printStackTrace();
        }
//        if( result != null ) {
//            message.reply( result );
//        } else {
//            message.reply( ErrorCode.SUCCESS.toNum() );
//        }
    }


    private void reportQueryError( Message<JsonObject> message, Throwable cause ){
        log.error( "Database query error", cause );
        message.fail( ErrorCode.DB_ERROR.ordinal(), cause.getMessage() );
    }
}
