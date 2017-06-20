package org.bbz.stock.quanttrader.core;

import lombok.extern.slf4j.Slf4j;
import org.bbz.stock.quanttrader.stock.StockTraderRecord;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liulaoye on 17-6-19.
 * 量化平台的上下文环境，核心关键数据都在里边
 */

@Slf4j
public class QuantTradeContext{

    public BigDecimal getTradeFee(){
        return tradeFee;
    }

    /**
     * 交易手续费
     */
    private BigDecimal tradeFee;
    /**
     * 持有的股票
     * String       stock ID
     * Integer      stock count
     */
    private Map<String, Integer> stocks;
    /**
     * 为整个模型准备的初始资金，永远不会被改变
     */
    private final BigDecimal initBalance;

    /**
     * 当前的可用资金
     */
    private BigDecimal currentBalance;

    /**
     * 成功的交易记录
     */
    private List<StockTraderRecord> traderRecords;

    public QuantTradeContext( String tradeFee, String initBalance ){
        this.tradeFee = new BigDecimal( tradeFee );
        this.initBalance = new BigDecimal( initBalance );
        this.currentBalance = new BigDecimal( initBalance );
        stocks = new HashMap<>();
        traderRecords = new ArrayList<>();
    }

    /**
     * 计算盈利，要减去手续费
     *
     * @param stockCurrentPrice 股票的当前价格
     * @return 盈利
     */
    public BigDecimal calcProfit( Map<String, BigDecimal> stockCurrentPrice ){

        BigDecimal amount = new BigDecimal( 0 );
        for( Map.Entry<String, Integer> entry : stocks.entrySet() ) {//计算拥有股票的市值
            if( stockCurrentPrice.containsKey( entry.getKey() ) ) {
                BigDecimal price = stockCurrentPrice.get( entry.getKey() );
                amount = amount.add( price.multiply( new BigDecimal( entry.getValue() ) ) );
//                amount += entry.getValue() * price;
            } else {
                log.error( "没有输入股票:" + entry.getKey() + "的当前价格" );
                return null;
            }
        }


        amount = amount.add( currentBalance );
//        amount += currentBalance;

        //计算手续费
//        amount -= calcFee();

        amount = amount.subtract( initBalance );
//        amount -= initBalance;
        return amount;
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
        if( traderRecord.getPrice().compareTo( new BigDecimal( 0 ) ) == -1 ) {
            log.error( "交易数量为0,或者价格小于等于0" );
            return;
        }

        BigDecimal amount = traderRecord.getPrice().multiply( new BigDecimal( traderRecord.getCount() ) );

        changeStockCount( traderRecord );
        currentBalance = currentBalance.subtract( amount );
//        currentBalance -= amount;

//        currentBalance -= Math.abs( amount ) * tradeFee;//减去手续费
        currentBalance = currentBalance.subtract( amount.abs().multiply( tradeFee ));//减去手续费
        traderRecords.add( traderRecord );
    }


    /**
     * 修改持仓股票数量
     *
     * @param traderRecord 交易记录
     */
    private void changeStockCount( StockTraderRecord traderRecord ){
        String stockId = traderRecord.getStockId();
        int changeCount = traderRecord.getCount();
        Integer oldCount = stocks.get( stockId );
        if( oldCount == null ) {
            oldCount = 0;
        }
        int newCount = oldCount + changeCount;
        if( newCount < 0 ) {
            throw new RuntimeException( "股票数量不能为负数" );
        }
        stocks.put( stockId, newCount );
    }


    public List<StockTraderRecord> getTraderRecords(){
        return traderRecords;
    }

    public int getStockCountById( String stockId ){
        return stocks.get( stockId );
    }
}
