package org.bbz.stock.quanttrader.consts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liulaoye on 17-7-19.
 * cong所有的股票原始信息
 */
public enum  AllStocks{
    INSTANCE;
    private Map<String ,String > allStocks = new HashMap<>(  );

    AllStocks( ){
        final List<String> lines;
        try {
            lines = Files.readAllLines( Paths.get( "resources/stocks.csv" ) );
            for( String line : lines ) {
                final String[] strings = line.split( "," );
                allStocks.put( strings[1],strings[2] );
            }
        } catch( IOException e ) {
            e.printStackTrace();
        }

    }
    public Map<String ,String > getAllStocks(){
        return allStocks;
    }

}
