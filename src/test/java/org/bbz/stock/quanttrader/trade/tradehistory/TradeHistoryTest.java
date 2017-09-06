//package org.bbz.stock.quanttrader.trade.tradehistory;
//
//import org.junit.Before;
//import org.junit.Test;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.util.Arrays;
//
//import static org.junit.Assert.assertEquals;
//
///**
// * Created by liu_k on 2017/6/22.
// * TradeHistoryTest
// */
//public class TradeHistoryTest{
//    private static String stockId = "600109";
//
//    @Before
//    public void setUp() throws Exception{
//        TradeHistory.INSTANCE.init();
//
//    }
//
//    @Test
//    public void attributeHistory() throws Exception{
//
//        String result[] = {"14.20", "13.91", "13.58", "13.43", "13.48", "13.30", "13.31", "12.90", "12.92", "12.73"};
//        LocalDate beginDate = LocalDate.parse( "2016-12-01" );
//        BigDecimal[] history = TradeHistory.INSTANCE.attributeHistory( stockId, beginDate, 10, null, StockUnitData.CLOSE );
//        System.out.println( Arrays.asList( history ) );
//        for( int i = 0; i < result.length; i++ ) {
//            assertEquals( history[i], new BigDecimal( result[i] ) );
//        }
//    }
//
//}