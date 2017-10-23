package org.bbz.stock.quanttrader.trade.core;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.bbz.stock.quanttrader.trade.stock.StockTradeRecord;

/**
 * Created by liulaoye on 17-6-20. <p> 投资组合<br/> <p> 包括股票以及现金
 */

@Data
@Slf4j
public class Portfolio {


  /**
   * 持有的股票 String       stock ID Integer      stock share
   */
  private Map<String, Integer> stocks = new HashMap<>();

  /**
   * 今日购买的股票，当日不能买卖
   */
  private Map<String, Integer> todayStocks;


  /**
   * 为整个模型准备的初始资金，永远不会被改变
   */
  private BigDecimal initBalance;

  /**
   * 当前的可用资金
   */
  private BigDecimal currentBalance;

  private final OrderCost orderCost;


  /**
   * 交易记录
   */
  private List<StockTradeRecord> tradeRecords;

  public Portfolio(String initBalance,
      OrderCost orderCost, List<StockTradeRecord> tradeRecords) {
    this(new BigDecimal(initBalance), orderCost, tradeRecords);
//        this.stocks = createStocks();
//        this.initBalance = initBalance;
//        this.currentBalance = currentBalance;
//        this.orderCost = orderCost;
//        this.stockPool = stockPool;
  }
//
//
//    Portfolio(String initBalance, OrderCost orderCost, Set<String> stockPool){
//
//        this(  new BigDecimal( initBalance ), new BigDecimal( initBalance ), orderCost,stockPool );
//    }


  Portfolio(BigDecimal initBalance, OrderCost orderCost, List<StockTradeRecord> tradeRecords) {
    init(tradeRecords);
//    this.stocks = createStocks(tradeRecords);
//    this.currentBalance = calcCurrentBalance(tradeRecords)
    this.initBalance = initBalance;
    this.currentBalance = initBalance;
//    this.currentBalance = currentBalance;
    this.orderCost = orderCost;
  }

  private void init(List<StockTradeRecord> tradeRecords) {
    for (StockTradeRecord tradeRecord : tradeRecords) {
      if (!tradeRecord.isPending()) {
        final String stock = tradeRecord.getStockId();
        Integer count = stocks.get(stock);
        count = count == null ? 0 : count;
        stocks.put(stock, count);
      }
    }
  }

  /**
   * 通过股票id返回数量，如果不存在返回0
   */
  public int getStockCountById(String stockId) {
    return stocks.getOrDefault(stockId, 0);
  }

  /**
   * 计算盈利，要减去手续费
   *
   * @param stockCurrentPrice 股票的当前价格
   * @return 盈利
   */
  public BigDecimal calcProfit(Map<String, BigDecimal> stockCurrentPrice) {

    BigDecimal amount = new BigDecimal(0);
    for (Map.Entry<String, Integer> entry : stocks.entrySet()) {//计算拥有股票的市值
      if (stockCurrentPrice.containsKey(entry.getKey())) {
        BigDecimal price = stockCurrentPrice.get(entry.getKey());
        amount = amount.add(price.multiply(BigDecimal.valueOf(entry.getValue())));

      } else {
        log.error("没有输入股票:" + entry.getKey() + "的当前价格");
        return null;
      }
    }
    amount = amount.add(currentBalance);
    amount = amount.subtract(initBalance);
    return amount;
  }

  /**
   * 修改持仓股票数量
   *
   * @param traderRecord 交易记录
   */
  private void changeStockCount(StockTradeRecord traderRecord) {
    String stockId = traderRecord.getStockId();
    int changeCount = traderRecord.getShare();

    Integer oldCount = stocks.getOrDefault(stockId, 0);

    int newCount = oldCount + changeCount;
    if (newCount < 0) {
      throw new RuntimeException("股票数量不能为负数");
    }
    stocks.put(stockId, newCount);
  }

  /**
   * 交易成功之后，修改持仓以及现金情况，请在调用处确定输入参数的合法性
   *
   * @param traderRecord 交易记录
   */
  void trade(StockTradeRecord traderRecord) {

    BigDecimal amount = new BigDecimal(traderRecord.getPrice() * traderRecord.getShare());

    changeStockCount(traderRecord);
    currentBalance = currentBalance.subtract(amount);//减去交易金额
    currentBalance = currentBalance.subtract(calcOrderCost(amount));//减去手续费
  }

  public String getStauts(Map<String, BigDecimal> priceMap) {
    return "盈利" + calcProfit(priceMap) + "，持仓 " + getStocks();
  }

  /**
   * 计算手续费，先做个简单版，再慢慢补充
   *
   * @param amount 交易金额，正为买入，负为卖出
   */
  public BigDecimal calcOrderCost(BigDecimal amount) {
//        System.out.println( amount.abs().multiply( orderCost.getCloseCommission() ));
    return amount.abs().multiply(orderCost.getCloseCommission());

  }

}
