package org.bbz.stock.quanttrader.model.impl.gridtrader;

import lombok.Data;
import org.bbz.stock.quanttrader.stock.StockTraderRecord;

import java.math.BigDecimal;

/**
 * Created by liukun on 2017/6/17.
 * 网格交易法的配置文件
 */
@Data
public class GridTraderCfg{


    /**
     * 基准价格
     */
    private float                           basePrice;

    /**
     * 最高价
     */
    private float                           highPrice;

    /**
     * 最低价
     */
    private float                           lowPrice;

    /**
     * 网格个数
     */
    private int                             gridNum;



    /**
     * 根据当前价格决定应该如何交易
     * @param stockId           股票id
     * @param currentPrice      当前价格
     * @return                  StockTraderEntity
     */
    public StockTraderRecord getStockTraderRecord( String stockId, BigDecimal currentPrice ){
        return new StockTraderRecord( stockId, 100, currentPrice );
    }
}
