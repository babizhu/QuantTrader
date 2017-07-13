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
public class StockTradeRecord{

    /**
     * 当次交易是否买入
     *
     * @return true:买入
     */
    public boolean isBuy(){
        return count > 0;
    }

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
     * 记录每个策略特殊的买卖点信息,不可能为null
     */
    private JsonObject attachement;

    /**
     *
     */
    public StockTradeRecord( String stockId, int count, BigDecimal price, JsonObject attachement ){
        this.stockId = stockId;
        this.count = count;
        this.price = price;
        this.attachement = attachement == null ? new JsonObject() : attachement;
        this.date = LocalDateTime.now();
        this.isSuccess = StockTraderStatus.PENDING;
    }


    public static StockTradeRecord create( String stockId, int count, BigDecimal price, JsonObject attachement ){
        return new StockTradeRecord( stockId, count, price, attachement );
    }

}
