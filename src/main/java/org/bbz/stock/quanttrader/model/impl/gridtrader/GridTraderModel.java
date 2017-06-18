package org.bbz.stock.quanttrader.model.impl.gridtrader;

import lombok.extern.slf4j.Slf4j;
import org.bbz.stock.quanttrader.account.StockAccount;
import org.bbz.stock.quanttrader.model.ITraderModel;
import org.bbz.stock.quanttrader.stock.StockTraderRecord;

/**
 * Created by liukun on 2017/6/17.
 * 网格交易法
 * 此量化交易模型又叫网格交易法，是一种通过假定股价未来一定时期内可能在某个箱体内波动，将资金等分成一定比例，然后根据股票的波动固定比例，进行不断的低吸高抛，不断产生盈利，积小胜成大胜的方法。
 * 这是一种区间套利、长期做下去一定是盈利的操盘方法。只要股价还在预定的范围内波动，
 * 严格遵循 “涨卖跌买”的原则操作，就会不断有差价可赚，不用担心“没钱买”或“没票卖”，是一种适合任何市道的必赚操作摸式：涨赚现金，跌赚股票。
 * https://www.joinquant.com/post/539
 */
@Slf4j
public class GridTraderModel implements ITraderModel{
    private final StockAccount account;
    private final GridTraderRuntimeParam para;
    private final GridTraderCfg cfg;


    public GridTraderModel( StockAccount account, GridTraderRuntimeParam para, GridTraderCfg cfg ){
        this.account = account;
        this.para = para;
        this.cfg = cfg;
    }


    /**
     * 判断是否需要交易
     *
     * @param currentPrice 当前股票的价格
     * @return true：需要交易
     */
    boolean needTrader( float currentPrice ){
        if( cfg.getBasePrice() < currentPrice ) {
            return true;
        }
        return false;
    }


    /**
     * 开始交易，由于目前没有交易api，修改为发微信或者邮件的方式提示
     */
    void trader( float currentPrice ){
        StockTraderRecord stockTraderRecord = cfg.getStockTraderRecord( para.getStockInfo().getId(), currentPrice );
        //TODO 调用交易api
        // 假设购买成功，修改运行时参数
        para.trade( stockTraderRecord );
        log.info( para.toString() );

    }

    @Override
    public void run(){
        float stockPrice = getStockPrice();
        if( needTrader( stockPrice ) ) {
            trader( stockPrice );
        }
    }

    private float getStockPrice(){
        return 34.3f;
    }
}
