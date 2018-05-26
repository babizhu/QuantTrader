package org.bbz.stock.quanttrader.database.service;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

public class TradeService extends AbstractDataServiceWithIdentity {

  private static final String TABLE_NAME = "trade";

  public TradeService(MongoClient mongoClient) {
    super(mongoClient, TABLE_NAME);
  }


  /**
   * 为运行trade从数据库查询必要的数据
   *
   * @param msg id
   */
  public void queryTradeArgument(Message<JsonObject> msg) {
//    我们最终的聚合查询匹配的评论，按照顺序排序，限制最新的二十条信息，连接用户的数据，扁平用户数组，最后只返回我们需要的必须数据，总的命令如下：
//
//    复制代码
//    db.post.aggregate([
//        { "$match": { "rating": "important" } },
//    { "$sort": { "date": -1 } },
//    { "$limit": 20 },
//    { "$lookup": {
//      "localField": "user_id",
//          "from": "user",
//          "foreignField": "_id",
//          "as": "userinfo"
//    } },
//    { "$unwind": "$userinfo" },
//    { "$project": {
//      "text": 1,
//          "date": 1,
//          "userinfo.modelName": 1,
//          "userinfo.country": 1
//    } }
//]);
//    复制代码
    JsonObject condition = msg.body();
//    db.trade.aggregate([{$lookup:{from: "tradingstrategy",localField:    "strategyId",foreignField: "_id", as: "stuffFromRight" }}])
//    db.trade.aggregate([{$lookup:{from: "tradingstrategy",localField:"strategyId",foreignField: "_id", as: "tradingstrategy" }},{$lookup:{from: "trade_records",localField:"_id",foreignField: "tradeId", as: "tradeRecords" }}])


    JsonArray pipeline = new JsonArray();
    JsonObject arg = new JsonObject().put("$lookup",
        new JsonObject().put("from", "tradingstrategy").put("localField", "strategyId")
            .put("foreignField", "_id").put("as", "strategy"));
    pipeline.add(arg);
//    {$lookup:{from: "trade_records",localField:"_id",foreignField: "tradeId", as: "tradeRecords" }}])

      arg = new JsonObject().put("$lookup",
          new JsonObject().put("from", "trade_records").put("localField", "_id")
              .put("foreignField", "tradeId").put("as", "tradeRecords"));
      pipeline.add(arg);

    JsonObject unwind = new JsonObject().put("$unwind", "$strategy");
    pipeline.add(unwind);

    JsonObject match = new JsonObject().put("$match", condition);
    pipeline.add(match);

    JsonObject command = new JsonObject()
        .put("aggregate", TABLE_NAME)
        .put("pipeline", pipeline);

    mongoClient.runCommand("aggregate", command, res -> {
      if (res.succeeded()) {
        msg.reply(res.result().getJsonArray("result"));
      } else {
        reportError(msg, res.cause());
      }
    });

  }

}