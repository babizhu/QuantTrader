package org.bbz.stock.quanttrader.stock;

import lombok.Data;

import java.time.Instant;

/**
 * Created by liukun on 2017/6/17.
 * 交易信息记录实体类
 */
@Data
public class StockTraderRecord{

    enum StockTraderStatus{
        PENDING,
        FALSE,
        TRUE;
    }
    /**
     * 交易日期
     */
    private Instant                 date;

    private String                  stockId;

    private int                     count;

    private float                   price;

    private boolean                 buy;

    private StockTraderStatus       isSuccess;

    public StockTraderRecord( String stockId, int count, float price, boolean isBuy ){
        this.stockId = stockId;
        this.count = count;
        this.price = price;
        this.buy = isBuy;
        this.isSuccess = StockTraderStatus.PENDING;
    }

}
