package org.bbz.stock.quanttrader.database.service;

import static org.bbz.stock.quanttrader.consts.JsonConsts.INIT_BALANCE_KEY;
import static org.bbz.stock.quanttrader.consts.JsonConsts.MONGO_DB_ID;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import org.bbz.stock.quanttrader.consts.JsonConsts;

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
  public void queryArgument(Message<JsonObject> msg) {
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
//          "userinfo.name": 1,
//          "userinfo.country": 1
//    } }
//]);
//    复制代码
    JsonObject condition = msg.body();
//    db.trade.aggregate([{$lookup:{from: "tradingstrategy",localField:    "strategyId",foreignField: "_id", as: "stuffFromRight" }}])
//    mongoClient.find(tableName, condition, res -> {
//      if (res.succeeded()) {
//        msg.reply(new JsonArray(res.result()));
////        log.info("记录条数：" + String.valueOf(res.result().size()));
//      } else {
//        reportError(msg, res.cause());
//      }
//    });
//    mongoClient.

    JsonArray pipeline = new JsonArray();
    JsonObject arg = new JsonObject().put("$lookup",
        new JsonObject().put("from", "tradingstrategy").put("localField", "strategyId")
            .put("foreignField", "_id").put("as", "strategy"));
    pipeline.add(arg);

    JsonObject unwind = new JsonObject().put("$unwind", "$strategy");
    pipeline.add(unwind);
    JsonObject project = new JsonObject().put("$project", new JsonObject()
        .put(MONGO_DB_ID,1)
        .put(JsonConsts.STOCK_LIST_KEY,1)
        .put("strategy.className",1)
        .put(INIT_BALANCE_KEY,1));
    pipeline.add(project);

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