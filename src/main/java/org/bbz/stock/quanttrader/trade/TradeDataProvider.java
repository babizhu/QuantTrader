package org.bbz.stock.quanttrader.trade;

import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.json.JsonObject;
import org.bbz.stock.quanttrader.consts.EventBusAddress;
import org.bbz.stock.quanttrader.consts.EventBusCommand;

public class TradeDataProvider {

  private final EventBus eventBus;

  public TradeDataProvider(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  protected void send(JsonObject msg, EventBusCommand cmd,
      Handler<Message<Object>> replyHandler) {
    DeliveryOptions options = new DeliveryOptions()
        .addHeader("action", cmd.name());
    eventBus.send(EventBusAddress.DB_ADDR, msg, options, reply -> {
      if (reply.succeeded()) {
        replyHandler.handle(reply.result());
      } else {
        ReplyException e = (ReplyException) reply.cause();
//        msg.reply(reportError(e.failureCode(), e.getMessage()));
      }
    });

  }

  private JsonObject reportError(int errorCode, String msg) {
    JsonObject resp = new JsonObject()
        .put("eid", errorCode)
        .put("msg", msg);
    return resp;
  }

  /**
   * 获取所有正在运行或者暂停的trade
   * @param replyHandler
   *
   */
  public void getAllStartedTrades(Handler<Message<Object>> replyHandler) {

    JsonObject condition = new JsonObject().put("status", 1);
    send(condition,EventBusCommand.DB_TRADE_ARGUMENT_QUERY, replyHandler);
  }
}
