package org.bbz.stock.quanttrader.trade;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.bbz.stock.quanttrader.consts.*;
import org.bbz.stock.quanttrader.trade.model.AbstractTradeModel;
import org.bbz.stock.quanttrader.trade.model.ITradeModel;
import org.bbz.stock.quanttrader.trade.model.TradeModelFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by liulaoye on 17-7-17. TradeVerticle
 */
@SuppressWarnings("unused")
@Slf4j
public class TradeVerticle extends AbstractVerticle {

  private static AtomicInteger index = new AtomicInteger(0);

//    保存属于本verticle(线程)的策略模型实例

//    private Map<String, Class<?>> tradeModelClassMap = new HashMap<>();
  /**
   * 正在运行的策略任务
   */
  private Map<String, ITradeModel> tradeModelTaskMap = new HashMap<>();
  private TradeDataProvider dataProvider;

  public void start(Future<Void> startFuture) throws Exception {
    EventBus eventBus = vertx.eventBus();
    String address = EventBusAddress.TRADE_MODEL_ADDR + index.getAndAdd(1);
    eventBus.consumer(address, this::onMessage);

    getAllTradesAndStart();

    log.info("TradeVerticle Started completed. Listen on " + address);
//        init();
  }

  @Override
  public void init(Vertx vertx, Context context) {
    super.init(vertx, context);
    dataProvider = new TradeDataProvider(vertx.eventBus());


  }

  /**
   * @param message message
   */
  private void onMessage(Message<JsonObject> message) {
    if (!message.headers().contains("action")) {
      message.fail(ErrorCode.NOT_IMPLENMENT.toNum(), "No action header specified");
    }
    String action = message.headers().get("action");
//    JsonObject arguments = message.body();

    try {
      switch (EventBusCommand.valueOf(action)) {
        case TRADE_START:
          startOneTrade(message);
          break;
        case TRADE_GET_INFO:
          getTradeInfo(message);
          break;
        case TRADE_RUNTIME_DETAIL:
          getTradeRuntimeDetail(message);
          break;
        case TRADE_PAUSE:
          pause(message);
          break;
        case TRADE_STOP:
          stop(message);
          break;
        default:
          message.fail(ErrorCode.BAD_ACTION.toNum(), "Bad action: " + action);
      }
    } catch (ErrorCodeException e) {
      message.fail(e.getErrorCode(), e.getMessage());
      e.printStackTrace();
    } catch (Exception e) {
      message.fail(ErrorCode.SYSTEM_ERROR.toNum(), e.toString());
      e.printStackTrace();
    }
//    //确保所有的调用都是同步返回的，否则下面的代码就没有意义，如果需要更新数据库只能到外层去做了吗？
//    if (result != null) {
//      message.reply(result);
//    } else {
//      message.reply(ErrorCode.SUCCESS.toNum());
//    }
  }

  /**
   * 获取指定ID的交易的各种运行时信息，包括交易id等其他信息
   */
  private void getTradeRuntimeDetail(Message<JsonObject> msg) {
    JsonObject arguments = msg.body();

    final String id = arguments.getString(JsonConsts.MONGO_DB_ID);
    final ITradeModel tradeModel = tradeModelTaskMap.get(id);

    if (tradeModel == null) {
      throw new ErrorCodeException(ErrorCode.Trade_NOT_START, id);
    }
    msg.reply(tradeModel.toJson());
  }


  private void getTradeInfo(Message<JsonObject> msg) {
    JsonObject arguments = msg.body();

    final String id = arguments.getString(JsonConsts.MONGO_DB_ID);
    final ITradeModel tradeModel = tradeModelTaskMap.get(id);

    if (tradeModel == null) {
      throw new ErrorCodeException(ErrorCode.Trade_NOT_START, id);
    }
    String lastInfo = tradeModel.getTradeInfo();
    msg.reply(new JsonObject().put("res", lastInfo));

  }

//    private void init() throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, ClassNotFoundException{
//        ClassPath classpath = ClassPath.from( Thread.currentThread().getContextClassLoader() ); // scans the class path used by classloader
//        for( ClassPath.ClassInfo classInfo : classpath.getTopLevelClassesRecursive( TRADE_MODEL_CLASS_PREFIX ) ) {
//            if( classInfo.load().getSuperclass().equals( org.bbz.stock.quanttrader.trade.model.AbstractTradeModel.class ) ) {
//                tradeModelClassMap.put( classInfo.getSimpleName(), classInfo.load() );
//            }
//        }
//    }

  private void run(long timerId) {
    for (ITradeModel tradeModel : tradeModelTaskMap.values()) {
      tradeModel.run();
    }

  }


  private void startOneTrade(Message<JsonObject> message)
  {


  }

//  /**
//   * 通过json配置信息启动一个策略模型
//   *
//   * @param msg 配置参数
//   */
//  private void startOneTrade(Message<JsonObject> msg)
//      throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
//    JsonObject argument = msg.body();
//    final String id = argument.getString(JsonConsts.MONGO_DB_ID);
//    if (tradeModelTaskMap.containsKey(id)) {
//      log.debug("交易【" + id + "】已经启动了");
//
//
//    } else {
//      final ITradeModel tradeModel = createTradeModel(argument);
//      tradeModelTaskMap.put(id, tradeModel);
////        vertx.setPeriodic( 30000, tradeModel::run );
//    }
//    msg.reply(ErrorCode.SUCCESS.toNum());
//  }
//
  private void pause(Message<JsonObject> msg) {
    JsonObject argument = msg.body();

    tradeModelTaskMap.remove(argument.getString(JsonConsts.MONGO_DB_ID));
    //更新数据库状态
  }

  private void stop(Message<JsonObject> msg) {
    JsonObject argument = msg.body();

    tradeModelTaskMap.remove(argument.getString(JsonConsts.MONGO_DB_ID));

    //更新数据库状态--删除相关数据
  }




  /**
   * 从数据库读取所有的交易信息到内存，并根据状态进行相应的操作
   */
  private void getAllTradesAndStart() {
    dataProvider.getAllStartedTrades(msg -> {

      JsonArray body = (JsonArray) msg.body();
      for (Object o : body) {
        JsonObject trade = (JsonObject) o;
        final AbstractTradeModel tradeModel = TradeModelFactory.createByJson(trade,vertx);
        if( tradeModel == null ){
          log.error("tradeModel生产失败，ID为 " + trade.getString(JsonConsts.MONGO_DB_ID));
          continue;
        }
        if (tradeModel.getStatus() == 1) {
          startOneTrade(tradeModel);
        }
      }
      vertx.setPeriodic(30000, this::run);
    });
  }

  private void startOneTrade(ITradeModel tradeModel) {
    tradeModelTaskMap.put(tradeModel.getId(),tradeModel);
  }

}