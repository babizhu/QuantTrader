package org.bbz.stock.quanttrader.trade.model;

import static org.bbz.stock.quanttrader.consts.Consts.TRADE_MODEL_CLASS_PREFIX;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bbz.stock.quanttrader.consts.JsonConsts;
import org.bbz.stock.quanttrader.trade.core.OrderCost;
import org.bbz.stock.quanttrader.trade.core.QuantTradeContext;
import org.bbz.stock.quanttrader.trade.stock.StockTradeRecord;
import org.bbz.stock.quanttrader.trade.stockdata.IStockDataProvider;
import org.bbz.stock.quanttrader.trade.stockdata.impl.TuShareDataProvider;

public class TradeModelFactory {

  public static AbstractTradeModel createByJson(JsonObject json, Vertx vertx) {
    final TradeModelFactory tradeModelFactory = new TradeModelFactory();
    try {
      return tradeModelFactory.createTradeModelByJson(json, vertx);
    } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
      e.printStackTrace();
    }
    return null;

  }


  private AbstractTradeModel createTradeModelByJson(JsonObject json, Vertx vertx)
      throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
    QuantTradeContext ctx = createQuantTradeContextByJson(json);
    final IStockDataProvider dataProvider = createDataProvider(
        json.getJsonObject(JsonConsts.DATA_PROVIDER_KEY), vertx);

    final JsonObject strategyJson = json.getJsonObject("strategy");
    String tradeModelClassName = getClassName(strategyJson.getString(JsonConsts.MODEL_CLASS_KEY));
    Class<?> clazz = Class.forName(tradeModelClassName);
    Constructor c = clazz.getConstructor(QuantTradeContext.class,
        IStockDataProvider.class, String.class, String.class, String.class, int.class,Set.class);
    final String id = json.getString(JsonConsts.MONGO_DB_ID);
    final String desc = json.getString(JsonConsts.MODEL_DESC_KEY);
    final String name = json.getString(JsonConsts.MODEL_NAME_KEY);
    final int status = json.getInteger(JsonConsts.MODEL_STATUS_KEY);

    final String stockList = json.getString(JsonConsts.STOCKS);
    Set<String> stockPool = new HashSet<>();
    for (String stock : stockList.split(",")) {
      stockPool.add(stock);
    }
    final AbstractTradeModel trade = (AbstractTradeModel) c
        .newInstance(ctx, dataProvider, name, id, desc, status,stockPool);

    return trade;
  }

  private IStockDataProvider createDataProvider(JsonObject dataProvider, Vertx vertx) {
    final HttpClientOptions httpClientOptions = new HttpClientOptions();
    String host = "localhost";
    if (dataProvider != null) {
      host = dataProvider.getString("host", "localhost");
    }
    httpClientOptions.setDefaultPort(8888).setDefaultHost(host).setConnectTimeout(4000)
        .setKeepAlive(true);
    return TuShareDataProvider.createShare(null, vertx.createHttpClient(httpClientOptions));
  }


  private QuantTradeContext createQuantTradeContextByJson(JsonObject json) {
    final BigDecimal closeTax = new BigDecimal(json.getString("closeTax", "0.001"));
    final BigDecimal openCommission = new BigDecimal(json.getString("openCommission", "0.0003"));
    final BigDecimal closeCommission = new BigDecimal(
        json.getString("closeCommission", "0.0003"));
    final BigDecimal minCommission = new BigDecimal(json.getString("minCommission", "5"));
    final OrderCost orderCost = new OrderCost(closeTax, openCommission, closeCommission,
        minCommission);
    final String initBalance = json
        .getString(JsonConsts.INIT_BALANCE_KEY, JsonConsts.DEFAULT_INIT_BALANCE_VALUE);

    final List<StockTradeRecord> tradeRecords = createStockTradeRecords(
        json.getJsonArray("tradeRecords"));

    final QuantTradeContext ctx = new QuantTradeContext(orderCost, initBalance, tradeRecords);

//    final HashMap<String, Integer> stocks = new HashMap<>();

//        final Map<String, String> allStocks = AllStocks.INSTANCE.getAllStocks();
//        for( String s : allStocks.keySet() ) {
//            stocks.put( s, 0 );, 0 );
//        }


    return ctx;
  }

  private List<StockTradeRecord> createStockTradeRecords(JsonArray tradeRecordsJson) {
    List<StockTradeRecord> tradeRecords = new ArrayList<>();
    for (Object o : tradeRecordsJson) {

      JsonObject tradeRecordJson = (JsonObject) o;
//      UUID id = UUID.fromString(o.getKey());
//      final JsonObject tradeRecordJson = (JsonObject) o.getValue();
      final StockTradeRecord stockTradeRecord = StockTradeRecord
          .MapperFromDB(UUID.fromString(tradeRecordJson.getString("id")),
              tradeRecordJson.getString("stock"), tradeRecordJson.getInteger("count")
              , tradeRecordJson.getFloat("price"), tradeRecordJson.getBoolean("isPending"),
              tradeRecordJson.getInteger("time")
          );

      tradeRecords.add(stockTradeRecord);
    }
    return tradeRecords;
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
//
//  private IStockDataProvider createDataProvider(JsonObject dataProvider) {
//    final HttpClientOptions httpClientOptions = new HttpClientOptions();
//    String host = "localhost";
//    if (dataProvider != null) {
//      host = dataProvider.getString("host", "localhost");
//    }
//    httpClientOptions.setDefaultPort(8888).setDefaultHost(host).setConnectTimeout(4000)
//        .setKeepAlive(true);
//    return TuShareDataProvider.createShare(null, vertx.createHttpClient(httpClientOptions));
//  }

}