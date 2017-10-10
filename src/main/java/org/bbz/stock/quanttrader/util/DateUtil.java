package org.bbz.stock.quanttrader.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by liulaoye on 17-7-13.
 */
public class DateUtil{

    public static String formatDate( LocalDate dt ){
        return dt.format( DateTimeFormatter.ofPattern( "yyyy-MM-dd" ) );
//        dt.format( DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm:ss" ) ) );
    }

    public static String formatDate( LocalDateTime dt ){
//        return dt.format( DateTimeFormatter.ofPattern( "yyyy-MM-dd" ) );
       return dt.format( DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm:ss" ) );
    }

    /**
     * 把时间字符串格式化为标准时间
     * 如果dt仅仅包含日期不包含时间，则设置小时、分钟、秒为0
     * 还有种情况，stock数据服务器会返回2017-06-15 10:30，也需要特殊处理
     * @param dt    返回的日期
     * @return
     */
    public static LocalDateTime parse(String dt ){
        LocalDateTime ret;
        if(dt.length() == 16){//2017-06-15 10:30
            dt += ":00";
            return LocalDateTime.parse( dt,DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm:ss" ) );
        }
        if( dt.length() < 11 ){
            ret =  LocalDateTime.of( LocalDate.parse( dt ), LocalTime.of( 0, 0, 0 ) );

        }else {
            ret = LocalDateTime.parse( dt,DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm:ss" ) );
        }
        return ret;
    }
}
