package org.bbz.stock.quanttrader.tradehistory;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by liu_k on 2017/6/20.
 * 历史交易数据
 */
public enum TradeHistory{
    INSTANCE;
    private Map<String, List<DayKLine>> dayKLineMap = new HashMap<>();

    public void init() throws IOException{
        List<String> strings = Files.readAllLines( Paths.get( "./resources/600109.txt" ) );
        List<DayKLine> dayKLines = strings.stream().map( v -> {
            String[] split = v.split( "," );
            LocalDate date = LocalDate.parse( split[0] );
            return new DayKLine( date,
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
        int lastIndex = TradeHistory.INSTANCE.dayKLineMap.get( stockId ).size() - 1;
        System.out.println( TradeHistory.INSTANCE.dayKLineMap.get( stockId ).get( lastIndex ) );
    }
}
