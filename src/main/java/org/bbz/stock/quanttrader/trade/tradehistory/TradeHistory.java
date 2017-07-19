package org.bbz.stock.quanttrader.trade.tradehistory;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by liu_k on 2017/6/20.
 * 历史交易数据
 */
public enum TradeHistory{
    INSTANCE;
    private Map<String, List<DayKBar>> dayKLineMap = new HashMap<>();

    public void init() throws IOException{
        List<String> strings = Files.readAllLines( Paths.get( "./resources/stocks.csv" ) );
        List<DayKBar> dayKLines = strings.stream().map( v -> {
            String[] split = v.split( "," );
            LocalDate date = LocalDate.parse( split[0] );
            return new DayKBar( date,
                    new BigDecimal( split[1] ),
                    new BigDecimal( split[2] ),
                    new BigDecimal( split[3] ),
                    new BigDecimal( split[4] ),
                    Integer.parseInt( split[5] ) );
        } ).collect( Collectors.toList() );
        dayKLineMap.put( "600109", dayKLines );

    }

    public static void main( String[] args ) throws IOException{
        String stockId = "600109";
        TradeHistory.INSTANCE.init();
//        int lastIndex = TradeHistory.INSTANCE.dayKLineMap.get( stockId ).size() - 1;
        TradeHistory.INSTANCE.dayKLineMap.get( stockId ).forEach( System.out::println );
//        System.out.println( TradeHistory.INSTANCE.dayKLineMap.get( stockId ).get( lastIndex ) );
    }

    public BigDecimal[] attributeHistory( String stockId,LocalDate beginDate, int count, TimeUnit unit, StockUnitData field ){
        if( count <= 0 ){
            throw new RuntimeException( "数量小于0" );
        }
        List<DayKBar> dayKLines = dayKLineMap.get( stockId );

        BigDecimal[] res = new BigDecimal[count];
        List<DayKBar> collect = dayKLines.stream().filter( v -> v.getDate().compareTo( beginDate ) >= 0 ).limit( count ).collect( Collectors.toList() );
//        System.out.println(collect);
        switch( field ) {
            case CLOSE:
                for( int i = 0; i < count; i++ ) {
                    res[i] = collect.get( i ).getClose();
                }
                break;
        }
        return res;

    }
}
