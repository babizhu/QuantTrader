package org.bbz.stock.quanttrader.trade.model;

/**
 * Created by liukun on 2017/6/17.
 * ITradeModel
 */
public interface ITradeModel{


    /**
     * 在整个策略的生命周期内仅仅执行一次，应当在数据库内记录此函数是否已经运行过
     */
    void initialize();

    /**
     * vertx会定时回调此函数,通常用于实战模拟
     */
    void run();


    /**
     * 每日开市前运行的函数
     */
    default void beforeOpen(){

    }

    /**
     * 每日收盘后运行的函数
     */
    default void afterClose(){

    }

    /**
     * 获取策略运行的最新信息
     * @return
     */
    String getTradeInfo();

    void refreshTradeRecords();
}
