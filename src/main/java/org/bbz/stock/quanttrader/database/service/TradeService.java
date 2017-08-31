package org.bbz.stock.quanttrader.database.service;

import io.vertx.ext.mongo.MongoClient;

public class TradeService extends AbstractDataServiceWithIdentity{
    private static final String USER_TABLE = "trade_record";

    public TradeService( MongoClient mongoClient ){
        super( mongoClient, USER_TABLE );
    }



}