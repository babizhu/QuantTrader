//package org.bbz.stock.quanttrader.backprobe;
//
//import org.bbz.stock.quanttrader.trade.core.OrderCost;
//import org.bbz.stock.quanttrader.trade.core.QuantTradeContext;
////import org.bbz.stock.quanttrader.trade.model.impl.simpletrade.SimpleTradeModel;
//import org.junit.Test;
//
//import java.time.LocalDateTime;
//import java.time.temporal.ChronoUnit;
//
///**
// * Created by liu_k on 2017/6/27.
// * test
// */
//public class BackProbeTest{
//    @Test
//    public void run() throws Exception{String stokcId = "600109";
//        QuantTradeContext ctx = new QuantTradeContext( new OrderCost(), "100000" );
//        SimpleTradeModel model = new SimpleTradeModel( ctx, stokcId );
//        BackProbe backProbe = new BackProbe( LocalDateTime.parse( "2017-06-01T00:00:00" ),
//                LocalDateTime.parse( "2017-06-07T00:00:00" ),
//                ChronoUnit.DAYS,
//                model );
//
//        backProbe.run();
//    }
//
//
//}