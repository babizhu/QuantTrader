package org.bbz.stock.quanttrader.trade.core;

import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.bbz.stock.quanttrader.trade.stock.StockTradeRecord;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by liulaoye on 17-6-19.
 * 量化平台的上下文环境，核心关键数据都在里边
 */

@Slf4j
public class QuantTradeContext{


    private final RunType runType;
//    set_order_cost(OrderCost(close_tax=0.001, open_commission=0.0003, close_commission=0.0003, min_commission=5), type='stock')
    /**
     * 交易手续费
     */
    private OrderCost orderCost;
    /**
     * 投资组合
     */
    private final Portfolio portfolio;

    /**
     * 成功的交易记录
     */
    private List<StockTradeRecord> traderRecords;

    public QuantTradeContext( OrderCost orderCost, String initBalance ){
        this.orderCost = orderCost;
        portfolio = new Portfolio( initBalance, orderCost );
        traderRecords = new ArrayList<>();
        runType = RunType.REAL_TRADE;
    }

    public Portfolio getPortfolio(){
        return portfolio;
    }

    /**
     * 用当前价格购买或者卖出股票
     * count>0      买入股票
     * count<0      卖出股票
     */
    public void order( String stockId, int count ){
        BigDecimal price = BigDecimal.valueOf( 3.45 );
        trade( StockTradeRecord.create( stockId, count, price, new JsonObject() ) );
    }

    /**
     * 用当前价格购买或者卖出股票
     * count>0      买入股票
     * count<0      卖出股票
     */
    public void order( String stockId, int count, JsonObject attachement ){

        BigDecimal price = BigDecimal.valueOf( 3.45 );
        trade( StockTradeRecord.create( stockId, count, price, attachement ) );
    }


    /**
     * 尝试购买股票到指定仓位，如果仓位已到，则不做任何操作
     * 对于买入来说，当前仓位大于要购买的仓位，则不做任何操作
     * 对于卖出来说，当前仓位小于要购买的仓位，则不做任何操作
     * count>0      买入股票
     * count<0      卖出股票
     *
     * @param stockId stockId
     * @param count   count
     * @param postion 要达到的仓位如0.1,0.5
     */
    public void tryOrder( String stockId, int count, float postion, JsonObject attachement ){
        BigDecimal price = BigDecimal.valueOf( 3.45 );
        trade( StockTradeRecord.create( stockId, count, price, attachement ) );
    }

    /**
     * 交易成功之后，修改持仓以及现金情况
     *
     * @param traderRecord 交易信息
     */
    void trade( StockTradeRecord traderRecord ){
        if( traderRecord == null ) {
            log.error( "交易记录不能为空" );
            throw new RuntimeException( "交易记录不能为空" );

        }
        if( traderRecord.getCount() == 0 ) {
            throw new RuntimeException( "交易数量不能为0" );

        }
        if( traderRecord.getPrice().compareTo( BigDecimal.valueOf( 0 ) ) <= 0 ) {
            throw new RuntimeException( "交易价格不能小于等于0" );
        }

        portfolio.trade( traderRecord );
        traderRecords.add( traderRecord );
    }


    public List<StockTradeRecord> getTraderRecords(){
        return traderRecords;
    }


    /**
     * 计算今日可卖的股票数量
     *
     * @param stockId stockId
     * @return
     */
    public int getCanSellStockCount( String stockId ){
        final List<StockTradeRecord> stocks = getTradeRecordsByStockId( stockId );
        return stocks.stream().filter( v -> !v.getDate().toLocalDate().equals( LocalDate.now() ) ).mapToInt( v -> v.getCount() ).sum();

    }

    public List<StockTradeRecord> getTradeRecordsByStockId( String stockId ){
        return traderRecords.stream().filter( v -> v.getStockId().equals( stockId ) ).collect( Collectors.toList() );
    }

    /**
     * 获取当前时间
     * 回测当中由回测系统指定时间
     * 真实交易则是当前时间
     *
     * @return
     */
    public LocalDate getCurrentDate(){
        if( runType == RunType.REAL_TRADE ) {
            return LocalDate.now();
        } else {
            return LocalDate.now();
        }
    }

    /**
     * 清仓
     * 由于T+1政策，因此有时候清仓会失败
     *
     * @param stockId
     * @return 成功卖出的股票
     */
    public int cleanUp( String stockId ){
        int stockNum = getCanSellStockCount( stockId );
        order( stockId, -stockNum );
        return stockNum;
    }
}
