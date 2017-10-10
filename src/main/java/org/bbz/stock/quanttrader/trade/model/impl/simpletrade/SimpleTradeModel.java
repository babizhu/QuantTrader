//package org.bbz.stock.quanttrader.trade.model.impl.simpletrade;
//
//import org.bbz.stock.quanttrader.trade.core.Portfolio;
//import org.bbz.stock.quanttrader.trade.core.QuantTradeContext;
//import org.bbz.stock.quanttrader.trade.model.AbstractTradeModel;
//
///**
// * Created by liu_k on 2017/6/20.
// * 最简单的交易模型例子
// */
//public class SimpleTradeModel extends AbstractTradeModel{
//    private final String stockId;
//
//    public SimpleTradeModel( QuantTradeContext ctx, String stockId ){
//        super(ctx, getId(), desc);
//        this.stockId = stockId;
//    }
//
//
//    @Override
//    public void initialize(){
//    }
//
//    @Override
//    @SuppressWarnings("unused")
//    public void run(){
//        Portfolio portfolio = ctx.getPortfolio();
//        if( portfolio.getStockCountById( stockId ) == 0 ) {
//            ctx.order( stockId, 1000,null );
//        } else {
//            ctx.order( stockId, -500,null );
//        }
//        System.out.println( portfolio );
//    }
//
//
//}
