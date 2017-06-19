package org.bbz.stock.quanttrader.model.impl.gridtrader;

import lombok.extern.slf4j.Slf4j;
import org.bbz.stock.quanttrader.stock.StockInfo;
import org.bbz.stock.quanttrader.stock.StockTraderRecord;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by liukun on 2017/6/18.
 * GridTraderRuntimeParamTest
 */
@SuppressWarnings("ConstantConditions")
@Slf4j
public class GridTraderRuntimeParamTest{
    /**
     * 股票交易手续费
     */
    private static final float FEE = 0.0003f;
    private static final String STOCK_ID = "6000345";

    /**
     * 生成一个网格模型的运行时参数，用于测试
     *
     * @return GridTraderRuntimeParam
     */
    private GridTraderRuntimeParam buildGridTraderRuntimeParam(){
        GridTraderRuntimeParam traderRuntimeParam = new GridTraderRuntimeParam();
        traderRuntimeParam.setInitBalance( 100000f );//初始资金10万
        traderRuntimeParam.setCurrentBalance( 100000f );//当前资金10万
        traderRuntimeParam.setStockInfo( new StockInfo( STOCK_ID, 0 ) );
        traderRuntimeParam.setTraderRecords( new ArrayList<>() );
        return traderRuntimeParam;
    }

    /**
     * Test 盈利计算
     */
    @SuppressWarnings("SameParameterValue")
    private void calcProfit( GridTraderRuntimeParam traderRuntimeParam, float currentStockPrice ){
        final List<StockTraderRecord> traderRecords = traderRuntimeParam.getTraderRecords();
        int stockCount = traderRecords.stream().mapToInt( v -> v.isBuy() ? v.getCount() : -v.getCount() ).sum();

        //计算盈利
        double profit = 0;
        for( StockTraderRecord traderRecord : traderRecords ) {
            double amount = traderRecord.getCount() * traderRecord.getPrice();
            double fee = amount * FEE;
            if( traderRecord.isBuy() ) {
                amount = -amount;
            }
            amount -= fee;
            profit += amount;
        }
        profit += currentStockPrice * stockCount;
        assertEquals( profit, traderRuntimeParam.calcProfit( currentStockPrice, FEE ), 0.001f );
        log.info( "当前价格：" + currentStockPrice + "，资金：" + traderRuntimeParam.getCurrentBalance() + "，股票数量：" +
                traderRuntimeParam.getStockInfo().getCount() +"，盈利：" + profit );
    }

    /**
     * Test 交易
     *
     * @throws Exception 要卖出的股票数量大于拥有的股票数量
     */
    @Test
    public void trade() throws Exception{

        GridTraderRuntimeParam traderRuntimeParam = buildGridTraderRuntimeParam();

        //初始化完毕
        log.info( traderRuntimeParam.toString() );
        boolean sell = false;
        boolean buy = true;
        List<StockTraderRecord> traderRecords = new ArrayList<>();
        traderRecords.add( new StockTraderRecord( STOCK_ID, 100, 5.21f, buy ) );
        traderRecords.add( new StockTraderRecord( STOCK_ID, 100, 5.31f, sell ) );
        traderRecords.add( new StockTraderRecord( STOCK_ID, 1000, 5.51f, buy ) );
        traderRecords.add( new StockTraderRecord( STOCK_ID, 100, 5.01f, sell ) );
        traderRecords.add( new StockTraderRecord( STOCK_ID, 100, 5.01f, sell ) );

        //交易完毕之后剩余股票数量
        int stockCount = traderRecords.stream().mapToInt( v -> v.isBuy() ? v.getCount() : -v.getCount() ).sum();

        for( StockTraderRecord traderRecord : traderRecords ) {
            traderRuntimeParam.trade( traderRecord );
        }

//        log.info( traderRuntimeParam.toString() );

        assertEquals( stockCount, traderRuntimeParam.getStockInfo().getCount() );
        assertEquals( traderRecords.size(), traderRuntimeParam.getTraderRecords().size() );
        calcProfit( traderRuntimeParam, 5.0f );

        try {
            traderRuntimeParam.trade( new StockTraderRecord( STOCK_ID, 100000, 5.21f, sell ) );
        } catch( Exception ex ) {
            assertTrue( ex instanceof RuntimeException );
            assertTrue( ex.getMessage().contains( "股票数量不能为负数" ) );
        }
    }



}