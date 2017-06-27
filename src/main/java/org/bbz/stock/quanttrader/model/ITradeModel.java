package org.bbz.stock.quanttrader.model;

/**
 * Created by liukun on 2017/6/17.
 * ITradeModel
 */
public interface ITradeModel{


    void initialize();

    /**
     * vertx会定时回调此函数
     * @param aLong         暂时我也不知道有什么用
     */
    void run( Long aLong );

    /**
     * 回测使用的方法
     * @param param         股票数据参数
     */
    void run( Object param );

    /**
     * 开市前运行的函数
     */
    default void beforeOpen(){

    }

    /**
     * 收盘后运行的函数
     */
    default void afterClose(){

    }

}
