package org.bbz.stock.quanttrader.trade.tradehistory;

/**
 * Created by liu_k on 2017/6/22.
 * 一个单位时间内的股票的数据
 */
public enum StockUnitData{
    /**
     * 时间段开始时价格
     */
    OPEN,
    /**
     *  时间段结束时价格
     */
    CLOSE,
    LOW,// 最低价
    HIGH,// 最高价
    VOLUME,//成交的股票数量
    MONEY,// 成交的金额
//    factor: 前复权因子, 我们提供的价格都是前复权后的, 但是利用这个值可以算出原始价格, 方法是价格除以factor, 比如: close/factor
    HIGH_LIMIT,// 涨停价
    LOW_LIMIT,// 跌停价
    AVG;//: 这段时间的平均价, 等于money/volume
//    price: 已经过时, 为了向前兼容, 等同于 avg
//    pre_close: 前一个单位时间结束时的价格, 按天则是前一天的收盘价, 按分钟则是前一分钟的结束价格
//    paused: bool值, 这只股票是否停牌, 停牌时open/close/low/high/pre_close依然有值,都等于停牌前的收盘价, volume=money=0
}
