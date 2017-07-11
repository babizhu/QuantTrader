package org.bbz.stock.quanttrader.trade.tradehistory;

import lombok.Data;

/**
 * Created by liu_k on 2017/6/20.
 * 日k线？哈哈哈
 *
 */
@Data
public class SimpleKBar{
//    private final LocalDateTime time;
    private final double    open;
    private final double    high;
    private final double    low;
    private final double    close;
    /**
     * 成交量
     */
    private final int           amount;
}
