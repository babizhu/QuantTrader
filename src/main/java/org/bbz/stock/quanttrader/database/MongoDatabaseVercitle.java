package org.bbz.stock.quanttrader.database;


import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import lombok.extern.slf4j.Slf4j;
import org.bbz.stock.quanttrader.consts.ErrorCode;
import org.bbz.stock.quanttrader.consts.EventBusAddress;
import org.bbz.stock.quanttrader.consts.EventBusCommand;
import org.bbz.stock.quanttrader.database.service.AuthService;
import org.bbz.stock.quanttrader.database.service.TradeService;
import org.bbz.stock.quanttrader.database.service.TradingStrategyService;
import org.bbz.stock.quanttrader.database.service.UserService;

/**
 * Created by liulaoye on 17-7-10. mongo db 数据库 vercitle
 */
@Slf4j
public class MongoDatabaseVercitle extends AbstractVerticle {

  private UserService userService;
  private TradeService tradeService;
  private AuthService authService;
  private TradingStrategyService tradingStrategyService;

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    JsonObject config = config();
    String uri = config.getString("uri", "mongodb://localhost:27017");
    String db = config.getString("db");

    JsonObject mongoconfig = new JsonObject()
        .put("connection_string", uri)
        .put("connectTimeoutMS", 5000)
        .put("serverSelectionTimeoutMS", 5000)
        .put("db_name", db);

    MongoClient mongoClient = MongoClient.createShared(vertx, mongoconfig);
    userService = new UserService(mongoClient);
    authService = new AuthService(mongoClient);
    tradeService = new TradeService(mongoClient);
    tradingStrategyService = new TradingStrategyService(mongoClient);
    vertx.eventBus().consumer(EventBusAddress.DB_ADDR, this::onMessage);
    log.debug("MongoDatabaseVercitle start() 执行完毕！");
    startFuture.complete();
  }

  private void onMessage(Message<JsonObject> message) {

    String action = message.headers().get("action");
    if (action == null) {
      message.fail(ErrorCode.NOT_IMPLENMENT.toNum(), "No action header specified");
    }
    try {
      switch (EventBusCommand.valueOf(action)) {
        case DB_USER_CREATE:
          userService.create(message);
          break;
        case DB_USER_UPDATE:
          userService.update(message);
          break;
        case DB_USER_DELETE:
          userService.delete(message);
          break;
        case DB_USER_QUERY:
          userService.query(message);
          break;
        case DB_ROLE_SAVE:
          authService.create(message);
          break;
        case DB_ROLE_QUERY:
          authService.query(message);
          break;
        case DB_TRADING_STRATEGY_CREATE:
          tradingStrategyService.create(message);
          break;
        case DB_TRADING_STRATEGY_UPDATE:
          tradingStrategyService.update(message);
          break;
        case DB_TRADING_STRATEGY_QUERY:
          tradingStrategyService.query(message);
          break;
        case DB_TRADING_STRATEGY_DELETE:
          tradingStrategyService.delete(message);
          break;
        case DB_TRADE_CREATE:
          tradeService.create(message);
          break;
        case DB_TRADE_UPDATE:
          tradeService.update(message);
          break;
        case DB_TRADE_QUERY:
          tradeService.query(message);
          break;
        case DB_TRADE_ARGUMENT_QUERY:
          tradeService.queryTradeArgument(message);
          break;
        case DB_TRADE_DELETE:
          tradeService.delete(message);
          break;
        default:
          message.fail(ErrorCode.BAD_ACTION.toNum(), "Bad action: " + action);
      }
    } catch (Exception e) {
      message.fail(ErrorCode.SYSTEM_ERROR.toNum(), e.toString());
      e.printStackTrace();
    }
  }
}
