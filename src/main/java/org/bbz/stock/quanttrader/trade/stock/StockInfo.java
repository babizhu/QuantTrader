package org.bbz.stock.quanttrader.trade.stock;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by liukun on 2017/6/17.
 * 股票信息实体
 */

@Data
@AllArgsConstructor
public class StockInfo{

//    AtomicInteger a;
    private String      id;
    /**
     * 股票的数量
     */
    private int         count;

    public void incrementCount( int count ){

        this.count += count;
    }

    public void decrementCount( int count ){
        this.count -= count;
        if( this.count < 0 ){
            throw new RuntimeException( "股票数量不能为负数" );
        }
    }


//    public void trade( StockTradeRecord traderRecord ){
//        if( traderRecord.isBuy() ){
//            share += traderRecord.getShare();
//
//        }
//    }
}
