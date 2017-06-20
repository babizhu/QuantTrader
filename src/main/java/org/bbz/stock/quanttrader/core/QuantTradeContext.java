package org.bbz.stock.quanttrader.core;

import lombok.extern.slf4j.Slf4j;
import org.bbz.stock.quanttrader.stock.StockTraderRecord;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liulaoye on 17-6-19.
 * 量化平台的上下文环境，核心关键数据都在里边
 */

@Slf4j
public class QuantTradeContext{


    /**
     * 交易手续费
     */
    private BigDecimal tradeFee;

    public Portfolio getPortfolio(){
        return portfolio;
    }

    /**
     * 投资组合
     */
    private final Portfolio portfolio;

    /**
     * 成功的交易记录
     */
    private List<StockTraderRecord> traderRecords;

    public QuantTradeContext( String tradeFee, String initBalance ){
        this.tradeFee = new BigDecimal( tradeFee );
        portfolio = new Portfolio( initBalance );
        traderRecords = new ArrayList<>();
    }

    /**
     * 用当前价格购买或者卖出股票
     * count>0      买入股票
     * count<0      卖出股票
     */
    public void order( String stockId, int count ){
        BigDecimal price = BigDecimal.valueOf( 3.45 );
        trade( StockTraderRecord.create( stockId, count, price ) );
    }

    /**
     * 交易成功之后，修改持仓以及现金情况
     *
     * @param traderRecord 交易信息
     */
    void trade( StockTraderRecord traderRecord ){
        if( traderRecord == null ) {
            log.error( "交易记录不能为空" );
            throw new RuntimeException( "交易记录不能为空" );

        }
        if( traderRecord.getCount() == 0 ){
            throw new RuntimeException( "交易数量不能为0" );

        }
        if( traderRecord.getPrice().compareTo( BigDecimal.valueOf( 0 ) ) <= 0 ) {
            throw new RuntimeException( "交易价格不能小于等于0" );
        }

        portfolio.trade( traderRecord, tradeFee );
        traderRecords.add( traderRecord );
    }


    public List<StockTraderRecord> getTraderRecords(){
        return traderRecords;
    }


    BigDecimal getTradeFee(){
        return tradeFee;
    }

}
