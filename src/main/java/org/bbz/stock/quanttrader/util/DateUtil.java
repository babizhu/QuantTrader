package org.bbz.stock.quanttrader.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by liulaoye on 17-7-13.
 */
public class DateUtil{

    public static String formatDate( LocalDate dt ){
        return dt.format( DateTimeFormatter.ofPattern( "yyyy-MM-dd" ) );
//        dt.format( DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm:ss" ) ) );
    }

    public static String formatDateTime( LocalDateTime dt ){
//        return dt.format( DateTimeFormatter.ofPattern( "yyyy-MM-dd" ) );
       return dt.format( DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm:ss" ) );
    }
}
