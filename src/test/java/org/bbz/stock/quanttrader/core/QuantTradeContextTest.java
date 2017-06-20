package org.bbz.stock.quanttrader.core;

import lombok.extern.slf4j.Slf4j;
import org.bbz.stock.quanttrader.stock.StockTraderRecord;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by liulaoye on 17-6-19.
 * QuantTradeContextTest
 */

@Slf4j
public class QuantTradeContextTest{
    private static final String FEE = "0.0003";
    private static final String INIT_BALANCE = "100000";
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
        Map<String, BigDecimal> currentPrice = buildCurrentStockPriceMap();
        BigDecimal amount = new BigDecimal( 0 );
        for( StockTraderRecord traderRecord : traderRecords ) {
            BigDecimal v = traderRecord.getPrice().multiply( new BigDecimal( traderRecord.getCount() ) );
            amount = amount.subtract( v );
            amount = amount.subtract( v.abs().multiply( tradeContext.getTradeFee() ) );
        }

        int stockCount = traderRecords.stream().filter( v -> v.getStockId().equals( STOCK_ID1 ) ).mapToInt( StockTraderRecord::getCount ).sum();
        amount = amount.add( currentPrice.get( STOCK_ID1 ).multiply( new BigDecimal( stockCount ) ) );

        stockCount = traderRecords.stream().filter( v -> v.getStockId().equals( STOCK_ID2 ) ).mapToInt( StockTraderRecord::getCount ).sum();
        amount = amount.add( currentPrice.get( STOCK_ID2 ).multiply( new BigDecimal( stockCount ) ) );


        stockCount = traderRecords.stream().filter( v -> v.getStockId().equals( STOCK_ID3 ) ).mapToInt( StockTraderRecord::getCount ).sum();
        amount = amount.add( currentPrice.get( STOCK_ID3 ).multiply( new BigDecimal( stockCount ) ) );


        assertEquals( amount, tradeContext.getPortfolio().calcProfit( currentPrice ) );
        log.info( "盈利：" + amount );
    }

    private Map<String, BigDecimal> buildCurrentStockPriceMap(){
        Map<String, BigDecimal> currentPrice = new HashMap<>();
        currentPrice.put( STOCK_ID1, new BigDecimal( "3" ) );
        currentPrice.put( STOCK_ID2, new BigDecimal( "2" ) );
        currentPrice.put( STOCK_ID3, new BigDecimal( "4.56" ) );
        return currentPrice;
    }

    @Test
    public void trade() throws Exception{
        final QuantTradeContext tradeContext = createQuantTradeContext();
        List<StockTraderRecord> traderRecords = new ArrayList<>();
        traderRecords.add( new StockTraderRecord( STOCK_ID1, 200, new BigDecimal( "2" ) ) );
        traderRecords.add( new StockTraderRecord( STOCK_ID2, 100, new BigDecimal( "2" ) ) );
        traderRecords.add( new StockTraderRecord( STOCK_ID1, -100, new BigDecimal( "1" ) ) );
        traderRecords.add( new StockTraderRecord( STOCK_ID3, 1000, new BigDecimal( "3" ) ) );
        traderRecords.add( new StockTraderRecord( STOCK_ID1, -100, new BigDecimal( "1.8" ) ) );
        traderRecords.add( new StockTraderRecord( STOCK_ID1, 100, new BigDecimal( "5" ) ) );

        traderRecords.forEach( tradeContext::trade );

        final Portfolio portfolio = tradeContext.getPortfolio();
        //交易完毕之后剩余股票数量
        int stockCount = traderRecords.stream().filter( v -> v.getStockId().equals( STOCK_ID1 ) ).mapToInt( StockTraderRecord::getCount ).sum();
        assertEquals( stockCount, portfolio.getStockCountById( STOCK_ID1 ) );

        stockCount = traderRecords.stream().filter( v -> v.getStockId().equals( STOCK_ID2 ) ).mapToInt( StockTraderRecord::getCount ).sum();
        assertEquals( stockCount, portfolio.getStockCountById( STOCK_ID2 ) );

        stockCount = traderRecords.stream().filter( v -> v.getStockId().equals( STOCK_ID3 ) ).mapToInt( StockTraderRecord::getCount ).sum();
        assertEquals( stockCount, portfolio.getStockCountById( STOCK_ID3 ) );

        assertEquals( traderRecords.size(), tradeContext.getTraderRecords().size() );

        calcProfit( tradeContext, traderRecords );
        tradeException( tradeContext );
    }

    /**
     * 测试一些非法的交易
     *
     * @param tradeContext
     */
    private void tradeException( QuantTradeContext tradeContext ){
        try {
            tradeContext.order( "6000432", 0 );
        } catch( Exception ex ) {
            assertTrue( ex.getMessage().contains( "交易数量不能为0" ) );
        }

        try {
            tradeContext.trade( StockTraderRecord.create( "300003", 100, BigDecimal.valueOf( -9 ) ) );
        } catch( Exception ex ) {
            assertTrue( ex.getMessage().contains( "交易价格不能小于等于0" ) );
        }
    }


}