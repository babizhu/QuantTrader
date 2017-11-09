package org.bbz.stock.quanttrader.http.handler.trade;

import static org.bbz.stock.quanttrader.consts.JsonConsts.MODEL_CLASS_KEY;

import com.google.common.base.Strings;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import org.bbz.stock.quanttrader.consts.ErrorCode;
import org.bbz.stock.quanttrader.consts.ErrorCodeException;
import org.bbz.stock.quanttrader.consts.EventBusAddress;
import org.bbz.stock.quanttrader.consts.EventBusCommand;
import org.bbz.stock.quanttrader.consts.JsonConsts;
import org.bbz.stock.quanttrader.http.handler.AbstractHandler;
import org.bbz.stock.quanttrader.http.handler.auth.anno.RequirePermissions;
import org.bbz.stock.quanttrader.http.handler.auth.anno.RequireRoles;

/**
 * 负责处理股票交易相关接口 2017-07-24 16:20 author:liulaoye
 */
@Slf4j
public class TradeHandler extends AbstractHandler {

  private static final int TRADE_MODEL_COUNT = 1;

  public TradeHandler(EventBus eventBus) {
    super(eventBus);

  }

  @Override
  public Router addRouter(Router restAPI) {
    restAPI.route("/start").handler(this::start);
    restAPI.route("/detail").handler(this::detail);
    restAPI.route("/myTrade").handler(this::myTrade);
//    restAPI.route("/getTradeInfo").handler(this::getTradeInfo);
    restAPI.route("/getTradeInfo").handler(this::getTradeInfo);
    restAPI.route("/save").handler(this::save);
    restAPI.route("/del").handler(this::del);
    restAPI.route("/query").handler(this::query);

    return restAPI;
  }

  private String calcTradeModelAddress(String id) {
    return EventBusAddress.TRADE_MODEL_ADDR + (id.hashCode() % TRADE_MODEL_COUNT);
  }

  /**
   * 某个交易的详情，通过id进行查询
   */
  private void detail(RoutingContext ctx) {
    final HttpServerRequest request = ctx.request();

    String id = request.getParam("id");
    if (Strings.isNullOrEmpty(id)) {
      throw new ErrorCodeException(ErrorCode.PARAMETER_ERROR, id + " is null");
    }

    JsonObject condition = new JsonObject().put(JsonConsts.MONGO_DB_ID, id);

    DeliveryOptions options = new DeliveryOptions()
        .addHeader("action", EventBusCommand.TRADE_RUNTIME_DETAIL.name());

    send(calcTradeModelAddress(id), condition, options, ctx,
        reply -> ctx.response().end(reply.body().toString()));
  }

  /**
   * 获取自己的交易记录
   */

  private void myTrade(RoutingContext ctx) {
//        final HttpServerRequest request = ctx.request();
    JsonObject condition = new JsonObject();
//
//        String id = request.getParam("id");
//        if (!Strings.isNullOrEmpty(id)) {
//            condition.put(JsonConsts.MONGO_DB_ID, id);
//        }
//        User user = ctx.user();
//        condition.put("user",user.principal());
//        condition.put("user",user.principal());
    DeliveryOptions options = new DeliveryOptions()
        .addHeader("action", EventBusCommand.DB_TRADE_QUERY.name());

    send(EventBusAddress.DB_ADDR, condition, options, ctx, reply ->
        ctx.response().end(reply.body().toString()));
  }

  private void query(RoutingContext ctx) {
    final HttpServerRequest request = ctx.request();
    JsonObject condition = new JsonObject();

    String id = request.getParam("id");
    if (!Strings.isNullOrEmpty(id)) {
      condition.put(JsonConsts.MONGO_DB_ID, id);
    }

    DeliveryOptions options = new DeliveryOptions()
        .addHeader("action", EventBusCommand.DB_TRADE_QUERY.name());

    send(EventBusAddress.DB_ADDR, condition, options, ctx, reply ->
        ctx.response().end(reply.body().toString()));
  }

  private void del(RoutingContext ctx) {

  }

  /**
   * 添加或者修改用户信息
   */
  private void save(RoutingContext ctx) {
    JsonObject tradeJson = ctx.getBodyAsJson();
    String postId = tradeJson.getString(JsonConsts.MONGO_DB_ID);

    boolean isCreate = postId.equals("-1");
    if (isCreate) {
      create(ctx, tradeJson);
    } else {
      update(ctx, tradeJson);
    }
  }

  private void update(RoutingContext ctx, JsonObject updateJson) {

    checkArguments(updateJson, "arguments", JsonConsts.STOCKS,
        "desc", "strategyId", JsonConsts.MONGO_DB_ID, JsonConsts.INIT_BALANCE_KEY);
    DeliveryOptions options = new DeliveryOptions()
        .addHeader("action", EventBusCommand.DB_TRADE_UPDATE.name());
    send(EventBusAddress.DB_ADDR, updateJson, options, ctx, reply -> {
      final JsonObject result = (JsonObject) reply.body();
      log.info(result.toString());
      ctx.response().end();
    });

  }

  private void create(RoutingContext ctx, JsonObject tradeJson) {
    checkArgumentsStrict(tradeJson, "name", "initBalance", "strategyId", "stocks",
        JsonConsts.MONGO_DB_ID, "userName", "arguments", "desc");

    tradeJson.put("status", 0);//增加状态指令

    tradeJson.remove(JsonConsts.MONGO_DB_ID);//去掉_id，以便让mongodb自动生成

    DeliveryOptions options = new DeliveryOptions()
        .addHeader("action", EventBusCommand.DB_TRADE_CREATE.name());
    send(EventBusAddress.DB_ADDR, tradeJson, options, ctx, reply -> {
      final String id = (String) reply.body();
      ctx.response().end(new JsonObject().put(JsonConsts.MONGO_DB_ID, id).toString());
    });
  }

  /**
   * 查看策略的运行情况
   */
  @RequirePermissions("sys:trade:query")
  @RequireRoles("user")
  private void getTradeInfo(RoutingContext ctx) {

    DeliveryOptions options = new DeliveryOptions()
        .addHeader("action", EventBusCommand.TRADE_GET_INFO.name());
    final String id = ctx.request().getParam(JsonConsts.MONGO_DB_ID);
    final JsonObject msg = new JsonObject().put(JsonConsts.MONGO_DB_ID, id);
    send(EventBusAddress.TRADE_MODEL_ADDR + "0", msg, options, ctx, reply -> {

      final JsonObject body = (JsonObject) reply.body();
//      String res = "<meta http-equiv=\"refresh\" content=\"10\">" + body.getString("res")
//          + "<br/><br/><br/><br/><br/><br/><br/>";
//      res += "<h3>L-L(ver 1.0)必发财炒股鸡</h3>";
////
//      ctx.response().setStatusCode(200)
//          .putHeader("content-type", "text/html; charset=utf-8").end(res);
      ctx.response().end(body.toString());
      log.info(body.toString());

    });
  }

  /**
   * 开始运行一个策略
   */
  private void start(RoutingContext ctx) {
    JsonObject tradeJson = ctx.getBodyAsJson();
    checkArguments(tradeJson, JsonConsts.MONGO_DB_ID);
    DeliveryOptions options = new DeliveryOptions()
        .addHeader("action", EventBusCommand.DB_TRADE_ARGUMENT_QUERY.name());

    send(EventBusAddress.DB_ADDR, tradeJson, options, ctx, reply ->
        {
          final JsonArray array = ((JsonArray) reply.body());
          if (array.isEmpty()) {
            reportError(ctx, ErrorCode.Trade_NOT_FOUND, tradeJson.getString(JsonConsts.MONGO_DB_ID));
            return;
          }
          final JsonObject res = array.getJsonObject(0);
          final JsonObject msg = new JsonObject().put(JsonConsts.CTX_KEY,
              new JsonObject()
                  .put(JsonConsts.INIT_BALANCE_KEY, res.getString(JsonConsts.INIT_BALANCE_KEY))
                  .put(JsonConsts.STOCKS, res.getString(JsonConsts.STOCKS)));
          msg.put(MODEL_CLASS_KEY, res.getJsonObject("strategy").getString(MODEL_CLASS_KEY));
          msg.put(JsonConsts.MONGO_DB_ID, tradeJson.getString(JsonConsts.MONGO_DB_ID));
//{"ctx":{"initBalance":"100000","stockList":"3000322"},"modelClass":"WaveTradeModel"}

          DeliveryOptions op = new DeliveryOptions()
              .addHeader("action", EventBusCommand.TRADE_START.name());

          send(EventBusAddress.TRADE_MODEL_ADDR + "0", msg, op, ctx,
              reply1 -> ctx.response().end(""));
        }
    );
  }
}
