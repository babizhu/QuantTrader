package org.bbz.stock.quanttrader.consts;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by liukun on 2017/7/11.
 * K线周期的枚举
 */

public enum KLineType{
    DAY( "D" ),
    MIN30( "30" ),
    MIN60( "60" ), WEEK( "W" );

    private final String kLineType;

    private static final Map<String, KLineType> stringToEnum = new HashMap<>();

    static{
        for( KLineType t : values() ) {

            KLineType s = stringToEnum.put( t.kLineType, t );
            if( s != null ) {
                throw new RuntimeException( t.kLineType + "重复了" );
            }
        }
    }

    KLineType( String kLineType ){
        this.kLineType = kLineType;
    }

    public String toStr(){
        return kLineType;
    }

    public static KLineType fromString( String kLineType ){
        return stringToEnum.get( kLineType );
    }
}

