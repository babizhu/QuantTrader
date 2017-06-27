package org.bbz.stock.quanttrader.backprobe;

import org.bbz.stock.quanttrader.model.ITradeModel;

import java.time.LocalDateTime;
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

    public BackProbe( LocalDateTime beginTime, LocalDateTime endTime, ChronoUnit frequency, ITradeModel model ){
        if( !frequency.equals( ChronoUnit.DAYS ) && frequency.equals( ChronoUnit.MINUTES ) ) {
            throw new RuntimeException( "回测时间单位仅支持天和分钟" );
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
            System.out.println( now );

        }
    }

}
