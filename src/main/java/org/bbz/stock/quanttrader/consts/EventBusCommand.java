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
     * 添加用户
     */
    DB_USER_SAVE,
    DB_USER_UPDATE,
    DB_USER_LOGIN,
    DB_USER_QUERY,

    /**
     * 权限管理
     */
    DB_ROLE_QUERY, DB_ROLE_SAVE,
}

