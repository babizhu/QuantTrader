package org.bbz.stock.quanttrader.trade;

import static org.bbz.stock.quanttrader.consts.Consts.TRADE_MODEL_CLASS_PREFIX;
import static org.bbz.stock.quanttrader.consts.JsonConsts.MODEL_CLASS_KEY;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.bbz.stock.quanttrader.consts.ErrorCode;
import org.bbz.stock.quanttrader.consts.ErrorCodeException;
import org.bbz.stock.quanttrader.consts.EventBusAddress;
import org.bbz.stock.quanttrader.consts.EventBusCommand;
import org.bbz.stock.quanttrader.consts.JsonConsts;
import org.bbz.stock.quanttrader.trade.core.OrderCost;
import org.bbz.stock.quanttrader.trade.core.Portfolio;
import org.bbz.stock.quanttrader.trade.core.QuantTradeContext;
import org.bbz.stock.quanttrader.trade.model.ITradeModel;
import org.bbz.stock.quanttrader.trade.stockdata.IStockDataProvider;
import org.bbz.stock.quanttrader.trade.stockdata.impl.TuShareDataProvider;

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
  private DBHandler db;

  public void start(Future<Void> startFuture) throws Exception {
    EventBus eventBus = vertx.eventBus();
    String address = EventBusAddress.TRADE_MODEL_ADDR + index.getAndAdd(1);
    eventBus.consumer(address, this::onMessage);

    startAllTrades();

    log.info("TradeVerticle Started completed. Listen on " + address);
//        init();
  }

  @Override
  public void init(Vertx vertx, Context context) {
    super.init(vertx, context);
    db = new DBHandler(vertx.eventBus());


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
   * 获取指定ID的交易的各种运行时信息
   */
  private void getTradeRuntimeDetail(Message<JsonObject> msg) {
    JsonObject arguments = msg.body();

    final String id = arguments.getString(JsonConsts.MONGO_DB_ID);
    final ITradeModel tradeModel = tradeModelTaskMap.get(id);

    if (tradeModel == null) {
      throw new ErrorCodeException(ErrorCode.Trade_NOT_START, id);
    }
    QuantTradeContext ctx = tradeModel.getQuantTradeContext();
    Portfolio portfolio = ctx.getPortfolio();
    JsonObject result = new JsonObject()
        .put("tradeDetail", new JsonObject().put(JsonConsts.TRADE_RECORDS, ctx.getTradeRecords()));

    msg.reply(result);
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

  private void startOneTrade(JsonObject trade)
      throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
    final String id = trade.getString(JsonConsts.MONGO_DB_ID);
    if (tradeModelTaskMap.containsKey(id)) {
      log.debug("交易【" + id + "】已经启动了");


    } else {
      final JsonObject msg = new JsonObject().put(JsonConsts.CTX_KEY,
          new JsonObject()
              .put(JsonConsts.INIT_BALANCE_KEY, trade.getString(JsonConsts.INIT_BALANCE_KEY))
              .put(JsonConsts.STOCKS, trade.getString(JsonConsts.STOCKS)));
      msg.put(MODEL_CLASS_KEY, trade.getJsonObject("strategy").getString(MODEL_CLASS_KEY));
      msg.put(JsonConsts.MONGO_DB_ID, trade.getString(JsonConsts.MONGO_DB_ID));
      final ITradeModel tradeModel = createTradeModel(msg);
      tradeModelTaskMap.put(id, tradeModel);
//        vertx.setPeriodic( 30000, tradeModel::run );
    }


  }

  /**
   * 通过json配置信息启动一个策略模型
   *
   * @param msg 配置参数
   */
  private void startOneTrade(Message<JsonObject> msg)
      throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
    JsonObject argument = msg.body();
    final String id = argument.getString(JsonConsts.MONGO_DB_ID);
    if (tradeModelTaskMap.containsKey(id)) {
      log.debug("交易【" + id + "】已经启动了");


    } else {
      final ITradeModel tradeModel = createTradeModel(argument);
      tradeModelTaskMap.put(id, tradeModel);
//        vertx.setPeriodic( 30000, tradeModel::run );
    }
    msg.reply(ErrorCode.SUCCESS.toNum());
  }

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
   * 创建ITradeModel实例
   *
   * @param argument 相关参数
   */
  private ITradeModel createTradeModel(JsonObject argument)
      throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
    QuantTradeContext ctx = createQuantTradeContext(argument.getJsonObject(JsonConsts.CTX_KEY));
    final IStockDataProvider dataProvider = createDataProvider(
        argument.getJsonObject(JsonConsts.DATA_PROVIDER_KEY));

    String tradeModelClassName = getClassName(argument.getString(JsonConsts.MODEL_CLASS_KEY));
    Class<?> clazz = Class.forName(tradeModelClassName);
    Constructor c = clazz.getConstructor(QuantTradeContext.class, IStockDataProvider.class);
    return (ITradeModel) c.newInstance(ctx, dataProvider);
  }


  /**
   * 获取完整的类名（包括完整包名）
   *
   * @param simpleClassName 简单的类名称
   * @return 添加包路径的类名称
   */
  private String getClassName(String simpleClassName) {
    String packageName = simpleClassName.toLowerCase().replace("model", "");
    return TRADE_MODEL_CLASS_PREFIX + "." + packageName + "." + simpleClassName;
  }

  private IStockDataProvider createDataProvider(JsonObject dataProvider) {
    final HttpClientOptions httpClientOptions = new HttpClientOptions();
    String host = "localhost";
    if (dataProvider != null) {
      host = dataProvider.getString("host", "localhost");
    }
    httpClientOptions.setDefaultPort(8888).setDefaultHost(host).setConnectTimeout(4000)
        .setKeepAlive(true);
    return TuShareDataProvider.createShare(null, vertx.createHttpClient(httpClientOptions));
  }

  /**
   * 生成QuantTradeContext实例
   *
   * @param ctxJson json内容
   * @return QuantTradeContext
   */
  private QuantTradeContext createQuantTradeContext(JsonObject ctxJson) {
    final BigDecimal closeTax = new BigDecimal(ctxJson.getString("closeTax", "0.001"));
    final BigDecimal openCommission = new BigDecimal(ctxJson.getString("openCommission", "0.0003"));
    final BigDecimal closeCommission = new BigDecimal(
        ctxJson.getString("closeCommission", "0.0003"));
    final BigDecimal minCommission = new BigDecimal(ctxJson.getString("minCommission", "5"));
    final OrderCost orderCost = new OrderCost(closeTax, openCommission, closeCommission,
        minCommission);
    final String initBalance = ctxJson
        .getString(JsonConsts.INIT_BALANCE_KEY, JsonConsts.DEFAULT_INIT_BALANCE_VALUE);
    final QuantTradeContext ctx = new QuantTradeContext(orderCost, initBalance);
    final String stockList = ctxJson.getString(JsonConsts.STOCKS);
    final HashMap<String, Integer> stocks = new HashMap<>();

//        final Map<String, String> allStocks = AllStocks.INSTANCE.getAllStocks();
//        for( String s : allStocks.keySet() ) {
////            stocks.put( s, 0 );, 0 );
//        }

    for (String stock : stockList.split(",")) {
      stocks.put(stock, 0);
    }
    ctx.getPortfolio().setStocks(stocks);
    return ctx;
  }

//  protected void send(String address, JsonObject msg, DeliveryOptions options, RoutingContext ctx,
//      Handler<Message<Object>> replyHandler) {
//    eventBus.send(address, msg, options, reply -> {
//      if (reply.succeeded()) {
//        replyHandler.handle(reply.result());
//      } else {
//        ReplyException e = (ReplyException) reply.cause();
////                ctx.response().setStatusCode( 500 ).end( e.failureCode() + "" );
////                ctx.response().setStatusCode( 500 ).end( e.toString() );
//        reportError(ctx, e.failureCode(), e.getMessage());
////                reply.cause().printStackTrace();
//      }
//    });
//
//  }

  private void startAllTrades() {
    db.getAllStartedTrades(msg -> {

      JsonArray body = (JsonArray) msg.body();
      for (Object o : body) {
        JsonObject trade = (JsonObject) o;
        try {
          startOneTrade(trade);
        } catch (ClassNotFoundException e) {
          e.printStackTrace();
        } catch (NoSuchMethodException e) {
          e.printStackTrace();
        } catch (InvocationTargetException e) {
          e.printStackTrace();
        } catch (InstantiationException e) {
          e.printStackTrace();
        } catch (IllegalAccessException e) {
          e.printStackTrace();
        }
      }
      vertx.setPeriodic(30000, this::run);
    });
  }

}
