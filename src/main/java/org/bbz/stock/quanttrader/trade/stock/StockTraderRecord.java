package org.bbz.stock.quanttrader.trade.stock;

import io.vertx.core.json.JsonObject;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Created by liukun on 2017/6/17.
 * 交易信息记录实体类
 */
@Data
public class StockTraderRecord{
    public static final String BUY_POINT_KTYPE = "BUY_POINT_KTYPE";//买入的k线是哪个单位，60分钟？30分钟？或者日k，卢哥的波浪大法需要记录

    enum StockTraderStatus{
        PENDING,
        FALSE,
        TRUE
    }

    /**
     * 交易日期
     */
    private LocalDateTime date;

    private String stockId;

    /**
     * 正数买进，负数卖出
     */
    private int count;

    private BigDecimal price;


    private StockTraderStatus isSuccess;
    /**
     * 记录每个策略特殊的买卖点信息
     */
    private JsonObject attachement;

    /**
     *
     */
    public StockTraderRecord( String stockId, int count, BigDecimal price, JsonObject attachement ){
        this.stockId = stockId;
        this.count = count;
        this.price = price;
        this.attachement = attachement;
        this.date = LocalDateTime.now();
        this.isSuccess = StockTraderStatus.PENDING;
    }


    public static StockTraderRecord create( String stockId, int count, BigDecimal price, JsonObject attachement ){
        return new StockTraderRecord( stockId, count, price, attachement );
    }

}
