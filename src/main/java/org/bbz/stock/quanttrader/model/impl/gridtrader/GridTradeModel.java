package org.bbz.stock.quanttrader.model.impl.gridtrader;

import lombok.extern.slf4j.Slf4j;
import org.bbz.stock.quanttrader.model.ITradeModel;

/**
 * Created by liukun on 2017/6/17.
 * 网格交易法
 * 此量化交易模型又叫网格交易法，是一种通过假定股价未来一定时期内可能在某个箱体内波动，将资金等分成一定比例，然后根据股票的波动固定比例，进行不断的低吸高抛，不断产生盈利，积小胜成大胜的方法。
 * 这是一种区间套利、长期做下去一定是盈利的操盘方法。只要股价还在预定的范围内波动，
 * 严格遵循 “涨卖跌买”的原则操作，就会不断有差价可赚，不用担心“没钱买”或“没票卖”，是一种适合任何市道的必赚操作摸式：涨赚现金，跌赚股票。
 * https://www.joinquant.com/post/539
 */
@Slf4j
public class GridTradeModel implements ITradeModel{


    @Override
    public void initialize(){

    }

    @Override
    public void run( Long aLong ){

    }

}
