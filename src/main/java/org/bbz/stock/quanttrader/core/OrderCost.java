package org.bbz.stock.quanttrader.core;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Created by liu_k on 2017/6/26.
 * 手续费
 * 股票类每笔交易时的手续费是：买入时佣金万分之三，卖出时佣金万分之三加千分之一印花税, 每笔交易佣金最低扣5块钱
 * close_tax=0.001, open_commission=0.0003, close_commission=0.0003, min_commission=5
 */
@Data
@AllArgsConstructor
public class OrderCost{
    private final BigDecimal closeTax = BigDecimal.valueOf( 0.001 );
    private final BigDecimal openCommission = BigDecimal.valueOf( 0.0003 );
    private final BigDecimal closeCommission = new BigDecimal( "0.0003" );
    private final BigDecimal minCommission = BigDecimal.valueOf( 5 );


}
