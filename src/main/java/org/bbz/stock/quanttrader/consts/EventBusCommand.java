package org.bbz.stock.quanttrader.consts;



/**
 * Created by   liu_k
 * Time         2015/8/10 17:19
 */

public enum EventBusCommand{

    /**
     * 运行一个策略
     */
    TRADE_RUN,
    /**
     * 策略最后一次的运行状况
     */
    TRADE_GET_INFO,
}

