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
