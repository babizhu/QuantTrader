package org.bbz.stock.quanttrader.database.service;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserService{
    private final MongoClient mongoClient;
    private final String USER_TABLE = "user";

    public UserService( MongoClient mongoClient ){
        this.mongoClient = mongoClient;
    }

    public void addUser( Message<JsonObject> msg ){
        final JsonObject user = msg.body();
        mongoClient.insert( USER_TABLE, user, res -> {
                    if( res.succeeded() ) {
                        msg.reply( res.result() );
                    } else {
                        res.cause().printStackTrace();
                    }
                }
        );

    }

    public void getAll( Message<JsonObject> msg ){
        JsonObject condition = msg.body();
        log.info( condition.toString() );
        mongoClient.find( USER_TABLE, condition, res -> {
                    if( res.succeeded() ) {
                        msg.reply( res.result().toString() );
                        log.info( "记录条数："+String.valueOf( res.result().size() ) );
                    } else {
                        res.cause().printStackTrace();
                    }
                }
        );

    }
}
