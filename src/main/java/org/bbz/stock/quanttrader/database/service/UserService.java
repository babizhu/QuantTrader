package org.bbz.stock.quanttrader.database.service;

import io.vertx.ext.mongo.MongoClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserService extends AbstractDataServiceWithIdentity{
    private static final String USER_TABLE = "user";

    public UserService( MongoClient mongoClient ){
        super( mongoClient, USER_TABLE );
    }




}
