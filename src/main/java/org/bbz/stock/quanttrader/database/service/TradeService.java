package org.bbz.stock.quanttrader.database.service;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

public class TradeService extends AbstractDataServiceWithIdentity{
    private static final String TABLE_NAME = "trade";

    public TradeService( MongoClient mongoClient ){
        super( mongoClient, TABLE_NAME);
    }


  /**
   * 为运行trade从数据库查询必要的数据
   * @param msg id
   */
  public void queryArgument(Message<JsonObject> msg) {
    JsonObject condition = msg.body();
    mongoClient.find(tableName, condition, res -> {
      if (res.succeeded()) {
        msg.reply(new JsonArray(res.result()));
//        log.info("记录条数：" + String.valueOf(res.result().size()));
      } else {
        reportError(msg, res.cause());
      }
    });
  }
}