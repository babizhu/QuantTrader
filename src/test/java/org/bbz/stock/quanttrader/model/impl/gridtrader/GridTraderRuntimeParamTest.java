package org.bbz.stock.quanttrader.model.impl.gridtrader;

import org.bbz.stock.quanttrader.stock.StockInfo;
import org.bbz.stock.quanttrader.stock.StockTraderRecord;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by liukun on 2017/6/18.
 *
 */
@SuppressWarnings("ConstantConditions")
public class GridTraderRuntimeParamTest{
    /**
     * 股票交易手续费
     */
    static float fee = 0.0003f;
    static String stockId = "6000345";

    /**
     * 生成一个网格模型的运行时参数，用于测试
     * @return
     */
    private GridTraderRuntimeParam buildGridTraderRuntimeParam(){
        GridTraderRuntimeParam traderRuntimeParam = new GridTraderRuntimeParam();
        traderRuntimeParam.setInitAmount( 100000f );//初始资金10万
        traderRuntimeParam.setCurrentAmount( 100000f );//当前资金10万
        traderRuntimeParam.setStockInfo( new StockInfo( stockId, 0 ) );
        traderRuntimeParam.setTraderRecords( new ArrayList<>() );
        return traderRuntimeParam;
    }

    /**
     * Test 盈利计算
     * @throws Exception
     */
    @Test
    public void calcProfit() throws Exception{
        GridTraderRuntimeParam traderRuntimeParam = buildGridTraderRuntimeParam();
        assertEquals( 0, traderRuntimeParam.calcProfit( 5.21f, fee ), 0f );

        boolean buy = true;
        traderRuntimeParam.trade( new StockTraderRecord( stockId, 2000, 5.21f, buy ) );//5.21买入1000股
        traderRuntimeParam.trade( new StockTraderRecord( stockId, 1000, 5.31f, !buy ) );//5.31卖出1000股
        traderRuntimeParam.trade( new StockTraderRecord( stockId, 1000, 5.31f, buy ) );//5.31买入1000股

        System.out.println( traderRuntimeParam.print( 5.41f, fee ) );
        assertEquals( 393.6875f, traderRuntimeParam.calcProfit( 5.41f, fee ), 0f );


    }

    /**
     * Test 交易
     * @throws Exception
     */
    @Test
    public void trade() throws Exception{

        GridTraderRuntimeParam traderRuntimeParam = buildGridTraderRuntimeParam();

        //初始化完毕
        System.out.println( traderRuntimeParam );

        boolean sell = false;
        boolean buy = true;
        traderRuntimeParam.trade( new StockTraderRecord( stockId, 100, 5.21f, buy ) );
        traderRuntimeParam.trade( new StockTraderRecord( stockId, 100, 5.31f, sell ) );
        traderRuntimeParam.trade( new StockTraderRecord( stockId, 1000, 5.51f, buy ) );
        traderRuntimeParam.trade( new StockTraderRecord( stockId, 100, 5.01f, sell ) );
        System.out.println( traderRuntimeParam );

        assertEquals( 900, traderRuntimeParam.getStockInfo().getCount() );
        assertEquals( 4, traderRuntimeParam.getTraderRecords().size() );
        try {
            traderRuntimeParam.trade( new StockTraderRecord( stockId, 1000, 5.21f, sell ) );
        } catch( Exception ex ) {
            assertTrue( ex instanceof RuntimeException );
            assertTrue( ex.getMessage().contains( "股票数量不能为负数" ) );
        }


    }

}