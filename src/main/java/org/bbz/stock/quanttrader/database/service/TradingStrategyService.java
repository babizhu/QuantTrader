package org.bbz.stock.quanttrader.database.service;

import io.vertx.ext.mongo.MongoClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TradingStrategyService extends AbstractDataServiceWithIdentity{
    private static final String TABLE = "tradingstrategy";

    public TradingStrategyService( MongoClient mongoClient ){
        super( mongoClient, TABLE );
    }




}
