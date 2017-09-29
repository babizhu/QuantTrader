package org.bbz.stock.quanttrader.consts;



/**
 * Created by   liu_k
 * Time         2015/8/10 17:19
 */

public enum EventBusCommand{

    /**
     * 运行一个策略
     */
    TRADE_START,
    /**
     * 策略最后一次的运行状况
     */
    TRADE_GET_INFO,

    /**
     * 用户
     */
    DB_USER_CREATE,
    DB_USER_UPDATE,
    DB_USER_DELETE,
    DB_USER_LOGIN,
    DB_USER_QUERY,

    /**
     * 交易策略
     */
    DB_TRADING_STRATEGY_CREATE,
    DB_TRADING_STRATEGY_QUERY,
    DB_TRADING_STRATEGY_DELETE,
    DB_TRADING_STRATEGY_UPDATE,

    DB_TRADE_CREATE,
    DB_TRADE_QUERY,
    DB_TRADE_DELETE,
    DB_TRADE_UPDATE,

    /**
     * 为运行trade获取相应准备数据
     */
    DB_TRADE_ARGUMENT_QUERY,
    /**
     * 权限管理
     */
    DB_ROLE_QUERY, DB_ROLE_SAVE,;

}

