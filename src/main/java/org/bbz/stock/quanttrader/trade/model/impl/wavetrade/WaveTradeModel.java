package org.bbz.stock.quanttrader.trade.model.impl.wavetrade;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.bbz.stock.quanttrader.consts.KLineType;
import org.bbz.stock.quanttrader.trade.core.Portfolio;
import org.bbz.stock.quanttrader.trade.core.QuantTradeContext;
import org.bbz.stock.quanttrader.trade.financeindicators.FinanceIndicators;
import org.bbz.stock.quanttrader.trade.model.AbstractTradeModel;
import org.bbz.stock.quanttrader.trade.stock.StockTradeRecord;
import org.bbz.stock.quanttrader.trade.stockdata.IStockDataProvider;
import org.bbz.stock.quanttrader.trade.tradehistory.SimpleKBar;
import org.bbz.stock.quanttrader.util.DateUtil;

/**
 * Created by liulaoye on 17-7-6. 卢哥 提供的策略
 */
@SuppressWarnings("unused")
@Slf4j
public class WaveTradeModel extends AbstractTradeModel {

  private final IStockDataProvider dataProvider;

  public WaveTradeModel(QuantTradeContext ctx, IStockDataProvider dataProvider, String modelName,
      String id, String desc, int status, Set<String> stockPool) {
    super(ctx, modelName, id, desc, status, stockPool);
    this.dataProvider = dataProvider;
  }

  @Override
  public void run() {
//    log.info("开始执行策略: " + DateUtil.formatDate(LocalDateTime.now()));
    final Portfolio portfolio = ctx.getPortfolio();
    for (String stock : getStockPool()) {
      if (portfolio.getStocks().containsKey(stock)) {//此股票在库存中
        tryCleanUp(stock).setHandler(res -> {
          if (!res.result()) {
            sellOrBuyInLittleWave(stock);//在小波浪中考虑加减仓
          }
        });
      } else {
        checkFirstBuy(stock);
      }
    }
    addLog(DateUtil.formatDate(LocalDateTime.now()) + "\r\n");
  }

  /**
   * 小波段中检测加减仓条件\r\n 上一次交易记录是买，这次才能卖\r\n 上一次交易记录是卖，这次才能买\r\n <p> 首次在哪个k线周期买入，以后所有的小波段操作都应该在这个k线周期完成\r\n
   */
  private void sellOrBuyInLittleWave(String stockId) {
    final JsonObject attachement = attachements.get(stockId);
    StockTradeRecord lastTradeRecord = ctx.getLastTradeRecord(stockId);
    KLineType kLineType = KLineType.fromString(attachement.getString(Consts.KLINE_TYPE_KEY));
    if (lastTradeRecord.isBuy()) {//最后一次操作是买入，所以本次操作应该是小波段减仓
      checkSellInLittleWave(stockId, kLineType);
    } else {//小波段加仓
      checkBuyInLittleWave(stockId, kLineType);
    }
  }

  /**
   * 检测首次买入
   *
   * @param stockId stockId
   */
  private void checkFirstBuy(String stockId) {
//    checkWeekUp(stockId)
//        .compose(res -> check60(stockId, true))根据卢哥要求，暂时去掉
    check60(stockId, true).setHandler(res -> {
      String result = stockId + " : ";
      if (res.failed()) {
        result += res.cause().getMessage();
      } else {
        result += "<font color=red>买入</font>";
        order(stockId);
        KLineType kLineType = res.result().getkLineType();
        setAttachement(stockId, Consts.KLINE_TYPE_KEY, kLineType.toStr());
//        setAttachement(stockId, Consts.FIRST_BUY_DATE_KEY,
//            DateUtil.formatDate(ctx.getCurrentDate()));
        setFirstCleanupPrice(stockId, LocalDate.now());
      }
      addLog(result + "\r\n");
    });
  }


  /**
   * 设置第一个清仓点
   *
   * @param date date
   */
  private void setFirstCleanupPrice(String stockId, LocalDate date) {
    dataProvider.getSimpleKBar(stockId, KLineType.DAY, 100, date.plusDays(-1), date, res -> {
      if (res.succeeded()) {
        setAttachement(stockId, Consts.CLEAN_UP_PRICE_KEY, res.result().get(0).getLow());
      } else {
        log.error(res.cause().getMessage());
      }
    });
  }

  private void checkSellInLittleWave(String stockId, KLineType kLineType) {
    dataProvider.getSimpleKBar(stockId, kLineType, 100, res -> {
      String result = stockId + " : ";
      if (res.succeeded()) {
        final List<SimpleKBar> kBars = res.result();
        if (KValueGreatThan(kBars, 80)) {//K值 > 80
          if (checkDown(kBars.subList(kBars.size() - 2, kBars.size()))) {
            result += "卖出";
//            ctx.order(stockId, -200);xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
            setAttachement(stockId, Consts.LAST_OP_IN_LITTLE_WAVE_KEY, Consts.SELL);
          } else {
            result += kLineType + "线未形成下摆,不能卖出";
          }
        } else {
          result += kLineType + "k值 < 80 ,不能卖出";
        }
      } else {
        result += res.cause().getMessage();
      }
      System.out.println(result);
      addLog(result + "\r\n");
    });
  }

  /**
   * 判断周线是否上摆(根据卢哥要求，暂时去掉)
   *
   * @param stockId 股票id
   * @return Future<Boolean>
   */
  private Future<Void> checkWeekUp(String stockId) {
    return Future.<List<SimpleKBar>>future(
        f -> dataProvider.getSimpleKBar(stockId, KLineType.WEEK, 2, f)
    ).compose(kBars -> {
      if (checkUp(kBars)) {
        return Future.succeededFuture();
      } else {
        return Future.succeededFuture();
//                return Future.failedFuture( "周线未形成上摆" );//临时修改为不考虑周线上摆
      }
    });
  }


  private void order(String stockId) {
    dataProvider.getCurrentKbar(stockId, bar -> {
      final float price = (float) bar.getClose();
      final Portfolio portfolio = ctx.getPortfolio();
      //第一次购买的股票数量=本金/股票池中的个数/价格
      int share = (int) (portfolio.getInitBalance().intValue() / getStockPool().size() / price);
      ctx.order(stockId, share, price);
    });
  }

  /**
   * 判断小波段的股票加仓的条件 外层确保此股票的当前数量为0
   *
   * @param stock stock id
   * @param kLineType 操作的K线周期
   */
  private void checkBuyInLittleWave(String stock, KLineType kLineType) {
    final Future<CheckResult> future;
    if (kLineType == KLineType.MIN60) {
      future = check60(stock, false);
    } else {
      future = check30(stock);
    }
    future.setHandler(res -> {
      String result = stock + " : ";
      if (res.failed()) {
        result += res.cause().getMessage();
      } else {
        result += "买入";
        order(stock);
        setAttachement(stock, Consts.LAST_OP_IN_LITTLE_WAVE_KEY, Consts.BUY);
      }
      System.out.println(result);
      addLog(result + "\r\n");
    });
  }

  /**
   * 检测60分钟k线的购买条件
   *
   * @param stockId stockId
   * @param needCheck30 60分钟检测失败，是否需要检测30分钟
   * @return 成功：Future.succeededFuture() 失败：Future.failedFuture( "失败原因" )
   */
  private Future<CheckResult> check60(String stockId, boolean needCheck30) {
    return Future.<List<SimpleKBar>>future(
        f -> dataProvider.getSimpleKBar(stockId, KLineType.MIN60, 100, f)
    ).compose(kBars -> {
      if (KValueLessThan(kBars, 35)) {
//              System.out.println("60分钟K值小于35，进入下一个检测");
        if (checkUp(kBars.subList(kBars.size() - 2, kBars.size()))) {//检测60分钟是否形成上摆
          return Future.succeededFuture(CheckResult.createResult(KLineType.MIN60));
        } else {
          if (needCheck30) {
            return check30(stockId);
          } else {
            return Future.failedFuture("60分钟K值 未小于 35");
          }
        }
      } else {
        return Future.failedFuture("60分钟K值 未小于 35");
      }
    });
  }

  /**
   * 检测30分钟k线的购买条件
   *
   * @param stockId stockId
   * @return 成功：Future.succeededFuture() 失败：Future.failedFuture( "失败原因" )
   */
  private Future<CheckResult> check30(String stockId) {
    return Future.<List<SimpleKBar>>future(
        f -> dataProvider.getSimpleKBar(stockId, KLineType.MIN30, 100, f)
    ).compose(kBars -> {
      if (KValueLessThan(kBars, 35)) {
        if (checkUp(kBars.subList(kBars.size() - 2, kBars.size()))) {//检测30分钟是否形成上摆
          return Future.succeededFuture(CheckResult.createResult(KLineType.MIN30));
        } else {
          return Future.failedFuture("30分钟K线未形成上摆");
        }
      } else {
        return Future.failedFuture("30分钟K值 未小于 35");
      }
    });
  }

  @Override
  public void beforeOpen() {
    System.out.println("WaveTradeModel.beforeOpen!!!!!!!!!!!!!!!!");
  }

  @Override
  public void afterClose() {
    log.info("开始执行盘后策略: " + DateUtil.formatDate(LocalDateTime.now()));
    final Portfolio portfolio = ctx.getPortfolio();
    for (Map.Entry<String, Integer> stock : portfolio.getStocks().entrySet()) {
      if (stock.getValue() != 0) {
        calcCleanPriceInBigWave(stock.getKey());
      }
    }
  }

  /**
   * 判断大波段的股票加仓和清仓价格
   *
   * @param stockId stockId
   */
  private void calcCleanPriceInBigWave(String stockId) {
    dataProvider.getSimpleKBar(stockId, KLineType.DAY, 100, res -> {
      if (res.succeeded()) {
        List<SimpleKBar> result = res.result();
        List<SimpleKBar> subList = result.stream()
            .filter(v -> v.getTime().toLocalDate().isAfter(LocalDate.parse("2017-06-06")))
            .collect(Collectors.toList());
        SimpleKBar current = subList.get(0);
        double lowPrice = Double.MAX_VALUE;
        double highPrice = Double.MIN_VALUE;

        boolean isDown = false;
        for (int i = 1; i < subList.size(); i++) {
          SimpleKBar temp = subList.get(i);
          if (current.getLow() > temp.getLow() && current.getHigh() > temp.getHigh()) { //下摆
            if (!isDown) {
              System.out.println("高点 ：" + highPrice);
            }
            isDown = true;
            lowPrice = Math.min(lowPrice, temp.getLow());//记录下摆中的最低点
            highPrice = Double.MIN_VALUE;
            current = temp;
          } else if (current.getLow() < temp.getLow() && current.getHigh() < temp.getHigh()) { //上摆
            if (isDown) {
              System.out.println(current.getTime() + " 低点 ：" + lowPrice);
            }
            isDown = false;
            highPrice = Math.max(highPrice, temp.getHigh());//记录下摆中的最低点
            lowPrice = Double.MAX_VALUE;
            current = temp;
          } else {
            if (isDown) {
              lowPrice = Math.min(lowPrice, temp.getLow());//记录下摆中的最低点
            } else {
              highPrice = Math.max(highPrice, temp.getHigh());//记录上摆中的最高点
            }
          }
        }
        setAttachement(stockId, Consts.CLEAN_UP_PRICE_KEY, lowPrice);
      } else {
        res.cause().printStackTrace();
      }

    });
  }

  /**
   * 检测D值是否小于某个数值
   *
   * @param data k线序列
   * @param v 要比较的值
   * @return true:   小于参数v          false:  大于参数v
   */
  @SuppressWarnings("SameParameterValue")
  private boolean KValueLessThan(List<SimpleKBar> data, double v) {
    double[][] doubles = FinanceIndicators.INSTANCE.calcKDJ(data, 8, 3, 2);
    int len = data.size() - 1;
    return doubles[0][len] < v;
  }

  /**
   * 检测kdj指标中的D值是否大于某个数值
   *
   * @param data k线序列
   * @param v 要比较的值
   * @return true:   大于参数v          false:  小于参数v
   */
  @SuppressWarnings("SameParameterValue")
  private boolean KValueGreatThan(List<SimpleKBar> data, double v) {
    double[][] doubles = FinanceIndicators.INSTANCE.calcKDJ(data, 8, 3, 2);
    int len = data.size() - 1;
//        System.out.println( "k:" + doubles[0][len] );
    return doubles[0][len] > v;
  }

  /**
   * 判断两个k bar 是否形成上摆
   *
   * @return true:   形成上摆 false:  未形成上摆
   */
  private boolean checkUp(List<SimpleKBar> data) {
    if (data.size() != 2) {
      log.error("判断上摆的数据不等于2个");
      return false;
    }
    final SimpleKBar oldSimpleKBar = data.get(0);
    final SimpleKBar newSimpleKBar = data.get(1);
    return oldSimpleKBar.getLow() < newSimpleKBar.getLow()
        && oldSimpleKBar.getHigh() < newSimpleKBar.getHigh();
  }

  /**
   * 判断两个k bar 是否形成下摆
   *
   * @return true:   形成下摆 false:  未形成上摆
   */
  private boolean checkDown(List<SimpleKBar> data) {
    if (data.size() != 2) {
      log.error("判断下摆的数据不等于2个,数量为：" + data.size());
      return false;
    }
    final SimpleKBar oldSimpleKBar = data.get(0);
    final SimpleKBar newSimpleKBar = data.get(1);
    return oldSimpleKBar.getLow() > newSimpleKBar.getLow()
        && oldSimpleKBar.getHigh() > newSimpleKBar.getHigh();
  }

//  /**
//   * 当前股价低于清仓价 尝试清仓
//   *
//   * @param stockId stockId
//   */
//  private void tryCleanUp1(String stockId) {
//    Double cleanupPrice = getDoubleFromAttachements(stockId, Consts.CLEAN_UP_PRICE_KEY);
//    if (cleanupPrice == null) {
//      return;
//    }
//    dataProvider.getCurrentKbar(stockId, res -> {
//      if (cleanupPrice > res.getClose()) {
//        System.out.println("当前价(" + res + ")低于清仓点：" + cleanupPrice + "。清仓卖出！！！");
//        ctx.cleanUp(stockId);
//      }
//    });
//  }

  private Future<Boolean> tryCleanUp(String stockId) {
    Double cleanupPrice = getDoubleFromAttachements(stockId, Consts.CLEAN_UP_PRICE_KEY);
    if (cleanupPrice == null) {
      return Future.succeededFuture(false);
    }
    Future<Boolean> future = Future.future();
    dataProvider.getCurrentKbar(stockId, res -> {
      if (cleanupPrice > res.getClose()) {
        System.out.println("当前价(" + res + ")低于清仓点：" + cleanupPrice + "。清仓卖出！！！");
        ctx.cleanUp(stockId);
        future.complete();
      }
    });
    return Future.future();
  }

}