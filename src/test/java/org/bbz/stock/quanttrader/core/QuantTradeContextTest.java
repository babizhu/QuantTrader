package org.bbz.stock.quanttrader.core;

import org.bbz.stock.quanttrader.stock.StockTraderRecord;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by liulaoye on 17-6-19.
 * QuantTradeContextTest
 */
public class QuantTradeContextTest{
    private static final float FEE = 0.0003f;
    private static final float INIT_BALANCE = 100000f;
    private static final String STOCK_ID1 = "6000345";
    private static final String STOCK_ID2 = "6000346";
    private static final String STOCK_ID3 = "6000347";

    private QuantTradeContext createQuantTradeContext(){
        return new QuantTradeContext( FEE, INIT_BALANCE );
    }

    /**
     * 验证盈利状况
     *
     * @param tradeContext  context
     * @param traderRecords 交易情况
     */
    private void calcProfit( QuantTradeContext tradeContext, List<StockTraderRecord> traderRecords ){
        Map<String, Float> currentPrice = buildCurrentPriceMap();
//        double fee = traderRecords.stream().mapToDouble( v->Math.abs( v.getCount() * v.getPrice() ) * tradeContext.getTradeFee() ).sum();
        double amount = 0;
        for( StockTraderRecord traderRecord : traderRecords ) {
            double v = traderRecord.getCount() * traderRecord.getPrice();
            amount -= v;
            amount -= Math.abs( v ) * tradeContext.getTradeFee();
        }

        int stockCount = traderRecords.stream().filter( v -> v.getStockId().equals( STOCK_ID1 ) ).mapToInt( StockTraderRecord::getCount ).sum();
        amount += stockCount * currentPrice.get( STOCK_ID1 );

        stockCount = traderRecords.stream().filter( v -> v.getStockId().equals( STOCK_ID2 ) ).mapToInt( StockTraderRecord::getCount ).sum();
        amount += stockCount * currentPrice.get( STOCK_ID2 );

        stockCount = traderRecords.stream().filter( v -> v.getStockId().equals( STOCK_ID3 ) ).mapToInt( StockTraderRecord::getCount ).sum();
        amount += stockCount * currentPrice.get( STOCK_ID3 );

        assertEquals( amount,tradeContext.calcProfit( currentPrice ),0f );
    }

    private Map<String, Float> buildCurrentPriceMap(){
        Map<String, Float> currentPrice = new HashMap<>();
        currentPrice.put( STOCK_ID1,2f );
        currentPrice.put( STOCK_ID2,3f );
        currentPrice.put( STOCK_ID3,4f );
        return currentPrice;
    }

    @Test
    public void trade() throws Exception{
        final QuantTradeContext tradeContext = createQuantTradeContext();
        List<StockTraderRecord> traderRecords = new ArrayList<>();
        traderRecords.add( new StockTraderRecord( STOCK_ID1, 200, 2f ) );
        traderRecords.add( new StockTraderRecord( STOCK_ID2, 100, 2f ) );
        traderRecords.add( new StockTraderRecord( STOCK_ID1, -100, 1f ) );
        traderRecords.add( new StockTraderRecord( STOCK_ID3, 1000, 3f ) );
        traderRecords.add( new StockTraderRecord( STOCK_ID1, -100, 1.8f ) );
        traderRecords.add( new StockTraderRecord( STOCK_ID1, 100, 5f ) );

        traderRecords.forEach( tradeContext::trade );

        //交易完毕之后剩余股票数量
        int stockCount = traderRecords.stream().filter( v -> v.getStockId().equals( STOCK_ID1 ) ).mapToInt( StockTraderRecord::getCount ).sum();
        assertEquals( stockCount, tradeContext.getStockCountById( STOCK_ID1 ) );

        stockCount = traderRecords.stream().filter( v -> v.getStockId().equals( STOCK_ID2 ) ).mapToInt( StockTraderRecord::getCount ).sum();
        assertEquals( stockCount, tradeContext.getStockCountById( STOCK_ID2 ) );

        stockCount = traderRecords.stream().filter( v -> v.getStockId().equals( STOCK_ID3 ) ).mapToInt( StockTraderRecord::getCount ).sum();
        assertEquals( stockCount, tradeContext.getStockCountById( STOCK_ID3 ) );

        assertEquals( traderRecords.size(), tradeContext.getTraderRecords().size() );

        calcProfit( tradeContext, traderRecords );
    }

}