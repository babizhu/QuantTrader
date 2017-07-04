package org.bbz.stock.quanttrader.tradehistory;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Created by liu_k on 2017/6/20.
 * 日k线？哈哈哈
 *
 */
@Data
public class DayKBar{
    private final LocalDate     date;
    private final BigDecimal    open;
    private final BigDecimal    high;
    private final BigDecimal    low;
    private final BigDecimal    close;
    /**
     * 成交量
     */
    private final int           amount;
}
