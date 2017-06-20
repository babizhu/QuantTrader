package org.bbz.stock.quanttrader.core;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.bbz.stock.quanttrader.stock.StockTraderRecord;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by liulaoye on 17-6-20.
 *
 * 投资组合<br/>
 *
 * 包括股票以及现金
 *
 */

@Data
@Slf4j
public class Portfolio{
    /**
     *
     * 持有的股票
     * String       stock ID
     * Integer      stock count
     */
    private Map<String, Integer> stocks;
    /**
     * 为整个模型准备的初始资金，永远不会被改变
     */
    private BigDecimal initBalance;

    /**
     * 当前的可用资金
     */
    private BigDecimal currentBalance;

    Portfolio( String initBalance ){

        this( new HashMap<>(), new BigDecimal( initBalance ), new BigDecimal( initBalance ) );
    }

    @SuppressWarnings("WeakerAccess")
    Portfolio( Map<String, Integer> stocks, BigDecimal initBalance, BigDecimal currentBalance ){
        this.stocks = stocks;
        this.initBalance = initBalance;
        this.currentBalance = currentBalance;
    }

    public int getStockCountById(String stockId ){
        return stocks.get( stockId );
    }
//
//
//
//    public BigDecimal changecurrentBalance(BigDecimal change){
//        currentBalance = currentBalance.add( change );
//        return currentBalance;
//    }

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

        amount = amount.subtract( initBalance  );
//        amount -= initBalance;
        return amount;
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

    /**
     * 交易成功之后，修改持仓以及现金情况，请在调用处确定输入参数的合法性
     * @param traderRecord          交易记录
     * @param tradeFee              用于计算手续费的参数
     */
    void trade( StockTraderRecord traderRecord,BigDecimal tradeFee ){

        BigDecimal amount = traderRecord.getPrice().multiply( new BigDecimal( traderRecord.getCount() ) );

        changeStockCount( traderRecord );
        currentBalance = currentBalance.subtract( amount );//减去交易金额
        currentBalance = currentBalance.subtract( amount.abs().multiply( tradeFee ));//减去手续费

    }

}
