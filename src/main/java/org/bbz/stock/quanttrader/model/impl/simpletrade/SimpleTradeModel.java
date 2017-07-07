package org.bbz.stock.quanttrader.model.impl.simpletrade;

import org.bbz.stock.quanttrader.core.Portfolio;
import org.bbz.stock.quanttrader.core.QuantTradeContext;
import org.bbz.stock.quanttrader.model.ITradeModel;

/**
 * Created by liu_k on 2017/6/20.
 * 最简单的交易模型例子
 */
public class SimpleTradeModel implements ITradeModel{
    private final QuantTradeContext ctx;
    private final String stockId;

    public SimpleTradeModel( QuantTradeContext ctx, String stockId ){
        this.ctx = ctx;
        this.stockId = stockId;
    }


    @Override
    public void initialize(){
    }

    @Override
    @SuppressWarnings("unused")
    public void run( Long aLong ){
        Portfolio portfolio = ctx.getPortfolio();
        if( portfolio.getStockCountById( stockId ) == 0 ) {
            ctx.order( stockId, 1000,null );
        } else {
            ctx.order( stockId, -500,null );
        }
        System.out.println( portfolio );
    }


}
