package org.bbz.stock.quanttrader.database.service;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AbstractDataServiceWithIdentity{

    private final MongoClient mongoClient;
    private final String tableName;

    public AbstractDataServiceWithIdentity( MongoClient mongoClient, String tableName ){
        this.mongoClient = mongoClient;
        this.tableName = tableName;
    }

    /**
     *
     */
    public void add( Message<JsonObject> msg ){
        final JsonObject user = msg.body();
        mongoClient.save( tableName, user, res -> {
            if( res.succeeded() ) {
                msg.reply( res.result() );
            } else {
                res.cause().printStackTrace();
            }
        });
    }

    public void query( Message<JsonObject> msg ){
        JsonObject condition = msg.body();
        log.info( condition.toString() );
        mongoClient.find( tableName, condition, res -> {
                    if( res.succeeded() ) {
                        msg.reply( res.result().toString() );
                        log.info( "记录条数：" + String.valueOf( res.result().size() ) );
                    } else {
                        res.cause().printStackTrace();
                    }
                }
        );

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
}
