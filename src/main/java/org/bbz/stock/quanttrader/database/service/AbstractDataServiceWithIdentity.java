package org.bbz.stock.quanttrader.database.service;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import lombok.extern.slf4j.Slf4j;
import org.bbz.stock.quanttrader.consts.ErrorCode;
import org.bbz.stock.quanttrader.consts.JsonConsts;

@Slf4j
public class AbstractDataServiceWithIdentity {

  protected final MongoClient mongoClient;
  protected final String tableName;

  AbstractDataServiceWithIdentity(MongoClient mongoClient, String tableName) {
    this.mongoClient = mongoClient;
    this.tableName = tableName;
  }

  public void delete(Message<JsonObject> msg) {
    JsonObject deleteField = msg.body();
    deleteField.put(JsonConsts.USER_NAME, new JsonObject().put("$ne", "admin"));//不能删除admin
    mongoClient.removeDocument(tableName, deleteField, res -> {
      if (res.succeeded()) {
        msg.reply(res.result().toJson());
      } else {
        reportError(msg, res.cause());
      }
    });
  }

  public void update(Message<JsonObject> msg) {
    JsonObject updateField = msg.body();
//    String id = updateField.getString(JsonConsts.MONGO_DB_ID);
    String id = (String) updateField.remove(JsonConsts.MONGO_DB_ID);

    mongoClient.updateCollection(tableName, new JsonObject().put(JsonConsts.MONGO_DB_ID, id),
        new JsonObject().put("$set", updateField), res -> {
          if (res.succeeded()) {
            msg.reply(res.result().toJson());
          } else {
            reportError(msg, res.cause());
          }

        });
  }


  public void create(Message<JsonObject> msg) {
    final JsonObject object = msg.body();

    mongoClient.save(tableName, object, res -> {
      if (res.succeeded()) {
        msg.reply(res.result());
      } else {
        reportError(msg, res.cause());

      }
    });
  }

  public void query(Message<JsonObject> msg) {
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


  protected void reportError(Message<JsonObject> message, Throwable cause) {
    log.error("Database query error", cause);
    message.fail(ErrorCode.DB_ERROR.toNum(), cause.getMessage());
  }
}
