package org.bbz.stock.quanttrader.database.service;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import lombok.extern.slf4j.Slf4j;
import org.bbz.stock.quanttrader.consts.ErrorCode;

@Slf4j
public class AbstractDataServiceWithIdentity{

    private final MongoClient mongoClient;
    private final String tableName;

    AbstractDataServiceWithIdentity( MongoClient mongoClient, String tableName ){
        this.mongoClient = mongoClient;
        this.tableName = tableName;
    }

    /**
     *
     */
    public void save( Message<JsonObject> msg ){
        final JsonObject object = msg.body();
        mongoClient.save( tableName, object, res -> {
            if( res.succeeded() ) {
                msg.reply( res.result() );
            } else {
                reportQueryError( msg, res.cause() );

            }
        } );
    }

    public void query( Message<JsonObject> msg ){
        JsonObject condition = msg.body();
        log.info( condition.toString() );
        mongoClient.find( tableName, condition, res -> {
            if( res.succeeded() ) {
                msg.reply( new JsonArray(  res.result()) );
                log.info( "记录条数：" + String.valueOf( res.result().size() ) );
            } else {
                reportQueryError( msg, res.cause() );
            }
        } );

    }

    /**
     * 如果数据库存在(id为标准)此数据，则更新此数据，否则添加此数据
     *
     * @param jo 要更新的数据
     * @return {@code true} 如果是替换
     */
    public boolean save( JsonObject jo ){
        return true;
    }

    private void reportQueryError( Message<JsonObject> message, Throwable cause ){
        log.error( "Database query error", cause );
        message.fail( ErrorCode.DB_ERROR.toNum(), cause.getMessage() );
    }
}
