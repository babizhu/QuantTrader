package org.bbz.stock.quanttrader.context;

import lombok.extern.slf4j.Slf4j;
import org.bbz.stock.quanttrader.stock.StockTraderRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liulaoye on 17-6-19.
 * 量化文件的上下文环境
 */

@Slf4j
public class QuantTradeContext{

    /**
     * 交易手续费
     */
    private float tradeFee;
    /**
     * 持有的股票
     * String       stock ID
     * Integer      stock count
     */
    private Map<String, Integer> stocks;
    /**
     * 为整个模型准备的初始资金，永远不会被改变
     */
    private float initBalance;

    /**
     * 当前的可用资金
     */
    private float currentBalance;

    /**
     * 成功的交易记录
     */
    private List<StockTraderRecord> traderRecords;

    public QuantTradeContext( float tradeFee, float initBalance ){
        this.tradeFee = tradeFee;
        this.initBalance = initBalance;
        this.currentBalance = initBalance;
        stocks = new HashMap<>();
        traderRecords = new ArrayList<>();
    }

    /**
     * 计算盈利，要减去手续费
     *
     * @param stockCurrentPrice 股票的当前价格
     * @return 盈利
     */
    public double calcProfit( Map<String, Float> stockCurrentPrice ){

        double amount = 0f;
        for( Map.Entry<String, Integer> entry : stocks.entrySet() ) {//计算拥有股票的市值
            if( stockCurrentPrice.containsKey( entry.getKey() ) ) {
                float price = stockCurrentPrice.get( entry.getKey() );
                amount += entry.getValue() * price;
            } else {
                log.error( "没有输入股票:" + entry.getKey() + "的当前价格" );
                return -1f;
            }
        }

        amount += currentBalance;

        //计算手续费
        amount -= calcFee( tradeFee );

        amount -= initBalance;
        return amount;
    }

    private double calcFee( float tradeFee ){
        return traderRecords.stream().mapToDouble( v -> v.getPrice() * v.getCount() * tradeFee ).sum();
    }


    /**
     * 交易成功之后，修改持仓以及现金情况
     *
     * @param traderRecord 交易信息
     */
    public void trade( StockTraderRecord traderRecord ){
        if( traderRecord == null ) {
            log.error( "交易记录不能为空" );
            return;
        }
        if( traderRecord.getCount() <= 0 || traderRecord.getPrice() <= 0f ) {
            log.error( "交易数量为0,或者价格小于等于0" );
            return;
        }

        float amount = traderRecord.getCount() * traderRecord.getPrice();
        if( !stocks.containsKey( traderRecord.getStockId() ) ) {
            stocks.put( traderRecord.getStockId(), traderRecord.getCount() );
        } else {

            if( traderRecord.isBuy() ) {
                incrementStockCount( traderRecord );
                currentBalance -= amount;
            } else {
                decrementCountStockCount( traderRecord );
                currentBalance += amount;
            }

        }
        currentBalance -= amount * tradeFee;//减去手续费
        traderRecords.add( traderRecord );
    }

    /**
     * 增加持仓股票数量
     *
     * @param traderRecord 交易记录
     */
    private void incrementStockCount( StockTraderRecord traderRecord ){
        final String stockId = traderRecord.getStockId();
        final Integer oldCount = stocks.get( stockId );
        stocks.put( stockId, oldCount + traderRecord.getCount() );
    }

    /**
     * 减少持仓股票数量
     *
     * @param traderRecord 交易记录
     */
    private void decrementCountStockCount( StockTraderRecord traderRecord ){
        String stockId = traderRecord.getStockId();
        int decrementCount = traderRecord.getCount();
        final Integer oldCount = stocks.get( stockId );
        if( oldCount - decrementCount < 0 ) {
            throw new RuntimeException( "股票数量不能为负数" );
        }
        stocks.put( stockId, oldCount - decrementCount );
    }


    public int getStockCount( String stockId ){
        return stocks.get( stockId );
    }
}
