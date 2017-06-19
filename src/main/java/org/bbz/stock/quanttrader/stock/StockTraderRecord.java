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
        TRUE
    }
    /**
     * 交易日期
     */
    private Instant                 date;

    private String                  stockId;

    /**
     * 正数买进，负数卖出
     */
    private int                     count;

    private float                   price;


    private StockTraderStatus       isSuccess;

    public StockTraderRecord( String stockId, int count, float price){
        this.stockId = stockId;
        this.count = count;
        this.price = price;
        this.isSuccess = StockTraderStatus.PENDING;
    }

}
