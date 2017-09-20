package org.bbz.stock.quanttrader.database.service;

import io.vertx.ext.mongo.MongoClient;

public class TradeService extends AbstractDataServiceWithIdentity{
    private static final String TABLE_NAME = "trade";

    public TradeService( MongoClient mongoClient ){
        super( mongoClient, TABLE_NAME);
    }



}