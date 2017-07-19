package org.bbz.stock.quanttrader.trade.core;

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
    private final BigDecimal closeTax;
    private final BigDecimal openCommission;
    private final BigDecimal closeCommission;
    private final BigDecimal minCommission;

    public OrderCost(){
        closeTax = new BigDecimal( "0.001" );
        openCommission = new BigDecimal( "0.0003" );
        closeCommission = new BigDecimal( "0.0003" );
        minCommission = new BigDecimal( "5" );
    }
}
