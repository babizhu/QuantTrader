package org.bbz.stock.quanttrader.database.service;

import io.vertx.ext.mongo.MongoClient;

public class AuthService extends AbstractDataServiceWithIdentity{
    private static final String USER_TABLE = "role_permissions";

    public AuthService( MongoClient mongoClient ){
        super( mongoClient, USER_TABLE );
    }


}
