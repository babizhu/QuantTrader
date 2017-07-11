package org.bbz.stock.quanttrader.trade.backprobe;

import org.bbz.stock.quanttrader.trade.model.ITradeModel;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

/**
 * Created by liu_k on 2017/6/26.
 * 回测实例
 */
public class BackProbe{

    private final LocalDateTime beginTime;
    private final LocalDateTime endTime;
    /**
     * 仅支持按天和分钟
     */
    private final ChronoUnit frequency;

    private final ITradeModel model;

    /**
     * 回测
     *
     * @param beginTime 开始时间
     * @param endTime   结束时间（不包括结束时间）
     * @param frequency 回测频率，按天和分钟
     * @param model     回测模型
     */
    public BackProbe( LocalDateTime beginTime, LocalDateTime endTime, ChronoUnit frequency, ITradeModel model ){
        if( !frequency.equals( ChronoUnit.DAYS ) && !frequency.equals( ChronoUnit.MINUTES ) ) {
            throw new RuntimeException( "回测时间单位仅支持天和分钟" );
        }
        if( beginTime.isAfter( endTime ) ) {
            throw new RuntimeException( "结束时间不能早于开始时间" );
        }
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.frequency = frequency;
        this.model = model;
    }

    public void run(){

        LocalDateTime now;
        int i = 0;

        while( (now = beginTime.plus( i++, frequency )).compareTo( endTime ) < 0 ) {
            run( now );
        }
    }

    /**
     * 过滤掉非股票交易时间
     *
     * @param now
     */
    private void run( LocalDateTime now ){
        LocalTime begin1 = LocalTime.parse( "09:29:00" );
        LocalTime end1 = LocalTime.parse( "11:31:00" );
        LocalTime begin2 = LocalTime.parse( "12:59:00" );
        LocalTime end2 = LocalTime.parse( "15:01:00" );
        LocalTime localTime = now.toLocalTime();

        if( frequency.equals( ChronoUnit.MINUTES ) ) {
            if( now.getDayOfWeek() == DayOfWeek.SATURDAY || now.getDayOfWeek() == DayOfWeek.SUNDAY ) {
                return;
            }
            if( (localTime.isAfter( begin1 ) && localTime.isBefore( end1 ) )
                    || (localTime.isAfter( begin2 ) && localTime.isBefore( end2 )) ) {
                System.out.println( now );
            }
        } else {
            if( now.getDayOfWeek() != DayOfWeek.SATURDAY && now.getDayOfWeek() != DayOfWeek.SUNDAY) {
                System.out.println( now );
            }
        }
    }
}
