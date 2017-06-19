package org.bbz.stock.quanttrader.context;

import org.bbz.stock.quanttrader.stock.StockTraderRecord;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by liulaoye on 17-6-19.
 * QuantTradeContextTest
 */
public class QuantTradeContextTest{
    private static final float FEE = 0.0003f;
    private static final float INIT_BALANCE = 100000f;
    private static final String STOCK_ID = "6000345";

    private QuantTradeContext createQuantTradeContext(){
        return new QuantTradeContext( FEE, INIT_BALANCE );
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void calcProfit() throws Exception{
        final QuantTradeContext tradeContext = createQuantTradeContext();
        boolean buy = true;
        List<StockTraderRecord> traderRecords = new ArrayList<>();
        traderRecords.add( new StockTraderRecord( STOCK_ID, 100, 5.21f, buy ) );
        traderRecords.add( new StockTraderRecord( STOCK_ID, 100, 5.31f, !buy ) );
        traderRecords.add( new StockTraderRecord( STOCK_ID, 1000, 5.51f, buy ) );
        traderRecords.add( new StockTraderRecord( STOCK_ID, 100, 5.01f, !buy ) );
        traderRecords.add( new StockTraderRecord( STOCK_ID, 100, 5.01f, !buy ) );

        traderRecords.forEach( tradeContext::trade );

        //交易完毕之后剩余股票数量
        int stockCount = traderRecords.stream().filter( v->v.getStockId().equals( STOCK_ID ) ).mapToInt( v -> v.isBuy() ? v.getCount() : -v.getCount() ).sum();
        assertEquals( stockCount, tradeContext.getStockCount( STOCK_ID ) );

    }

    @Test
    public void trade() throws Exception{
    }

}