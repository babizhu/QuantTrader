package org.bbz.stock.quanttrader.trade.stockdata;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import org.bbz.stock.quanttrader.consts.KLineType;
import org.bbz.stock.quanttrader.trade.tradehistory.SimpleKBar;

import java.time.LocalDate;
import java.util.List;

/**
 * Created by liulaoye on 17-7-6.
 * IStockDataProvider
 */
public interface IStockDataProvider{
    /*
     * 通过web接口从tushare返回需要的数据
     * stockId：股票代码，即6位数字代码，或者指数代码（sh=上证指数 sz=深圳成指 hs300=沪深300指数 sz50=上证50 zxb=中小板 cyb=创业板）
     * ktype：数据类型，D=日k线 W=周 M=月 5=5分钟 15=15分钟 30=30分钟 60=60分钟，默认为D
     * start：开始日期，格式YYYY-MM-DD              暂时未使用
     * end：结束日期，格式YYYY-MM-DD                暂时未使用
     * retry_count：当网络异常后重试次数，默认为3     暂时未使用
     * pause:重试时停顿秒数，默认为0
     *
     * 注意：
      如果指定了start或者end，即使end日期远大于今日日期，返回的数据不含今日的k线数据，这个需要修正一下
     */
    void getSimpleKBarExt( String stockId, KLineType kLineTypeType, int count, Handler<AsyncResult<List<SimpleKBar>>> resultHandler );

    /**
     * 获取指定日期的K线周期指标
     * @param stockId           stockId
     * @param kLineType         K线类型
     * @param count             数量
     * @param start             开始日期
     * @param end               结束日期
     * @param resultHandler     回调
     */
    void getSimpleKBarExt( String stockId, KLineType kLineType, int count,
                           LocalDate start,
                           LocalDate end,
                           Handler<AsyncResult<List<SimpleKBar>>> resultHandler );


    /**
     * 获取当前k bar
     * @param stockId           stockId
     * @param resultHandler     回调
     */
    void getCurrentKbar( String stockId, Handler<AsyncResult<SimpleKBar>> resultHandler  );
}
