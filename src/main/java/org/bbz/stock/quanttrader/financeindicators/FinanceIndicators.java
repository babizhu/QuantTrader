package org.bbz.stock.quanttrader.financeindicators;

import org.bbz.stock.quanttrader.tradehistory.SimpleKBar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liukun on 2017/7/3.
 * FinanceIndicators 财务指标计算
 */
public enum FinanceIndicators{
    INSTANCE;

    public final Double calcEXPMA( final List<Double> list, final int number ){
        // 开始计算EMA值，
        Double k = 2.0 / (number + 1.0);// 计算平滑系数
        Double ema = list.get( 0 );// 第一天ema等于当天收盘价
        for( int i = 1; i < list.size(); i++ ) {
            // 第二天以后，当天收盘 收盘价乘以系数再加上昨天EMA乘以系数-1
//            ema = list.get( i ) * k + ema * (1 - k);
            ema = (list.get( i ) - ema) * k + ema;
        }
        return ema;
    }

    /**
     * calculate MACD values
     *
     * @param list        :Price list to calculate，the first at head, the last at tail.
     * @param shortPeriod :the short period value.
     * @param longPeriod  :the long period value.
     * @param midPeriod   :the mid period value.
     * @return macd
     */
    public final Map<String, Double> calcMACD( final List<Double> list, final int shortPeriod, final int longPeriod, int midPeriod ){
        HashMap<String, Double> macdData = new HashMap<>();
        List<Double> diffList = new ArrayList<>();
        Double shortEMA;
        Double longEMA;
        Double dif = 0.0;
        Double dea;


        for( int i = list.size() - 1; i >= 0; i-- ) {
            List<Double> sublist = list.subList( 0, list.size() - i );
            shortEMA = calcEXPMA( sublist, shortPeriod );
            longEMA = calcEXPMA( sublist, longPeriod );
            dif = shortEMA - longEMA;
            diffList.add( dif );
        }
        dea = calcEXPMA( diffList, midPeriod );
        macdData.put( "DIF", dif );
        macdData.put( "DEA", dea );
        macdData.put( "MACD", (dif - dea) * 2 );
        return macdData;
    }

    /**
     * 计算kdj指标
     *
     * @param list 股票收盘价
     */
//    public final double calcKDJ( final List<SimpleKBar> list ){
//        return 0.0;
//    }

    public final double[][] calcKDJ( final List<SimpleKBar> list ){
        double[] high = list.stream().mapToDouble( SimpleKBar::getHigh ).toArray();
        double[] low = list.stream().mapToDouble( SimpleKBar::getLow ).toArray();
        double[] close = list.stream().mapToDouble( SimpleKBar::getClose ).toArray();
        double outSlowK[] = new double[high.length];
        double outSlowD[] = new double[high.length];
        double outSlowJ[] = new double[high.length];
        int length = list.size();
        double[] RSV = new double[length];
        for( int i = 0; i < length; i++ ) {
            if( i >= 8 ) {
                int start = i - 8;
                double high9 = Double.MIN_VALUE;
                double low9 = Double.MAX_VALUE;
                while( start <= i ) {
                    if( high[start] > high9 ) {
                        high9 = high[start];
                    }
                    if( low[start] < low9 ) {
                        low9 = low[start];
                    }
                    start++;
                }
                RSV[i] = (close[i] - low9) / (high9 - low9) * 100;
            } else {
                RSV[i] = 0d;
            }
        }
        for( int i = 0; i < length; i++ ) {
            if( i > 1 ) {
                outSlowK[i] = 2d / 3d * outSlowK[i - 1] + 1d / 3d * RSV[i];
                outSlowD[i] = 2d / 3d * outSlowD[i - 1] + 1d / 3d * outSlowK[i];
                outSlowJ[i] = 3d * outSlowK[i] - 2d * outSlowD[i];

//                if( outSlowJ[i] > 100 ) {
//                    System.out.println( "J值 > 100");
//                    outSlowJ[i] = 100;
//                } else if( outSlowJ[i] < 0 ) {
//                    System.out.println( "J值 < 0");
//                    outSlowJ[i] = 0;
//                }
            } else {
                outSlowK[i] = 50;
                outSlowD[i] = 50;
                outSlowJ[i] = 50;
            }
        }

        return new double[][]{outSlowK, outSlowD, outSlowJ};
    }

//    private final double calcRSVPerDay( final List<SimpleKBar> list ){
////        n日RSV=（Cn－Ln）/（Hn－Ln）×100
//        Double max = list.stream().map( v -> v.getHigh() ).max( ( o1, o2 ) -> o1 > o2 ? 1 : -1 ).get();
//        System.out.println( max );
//        Double min = list.stream().map( v -> v.getLow() ).min( ( o1, o2 ) -> o1 > o2 ? 1 : -1 ).get();
//        System.out.println( min );
//        double ret = (list.get( list.size() - 1 ).getClose() - min) / (max - min) * 100;
//
//        double k = 50;
//        for( int i = 1; i < list.size(); i++ ) {
//            k = (2 / 3) * k + (1 / 3) * ret;
//
////        }
//
//        System.out.println(ret );
//        return ret;
//
//}
}

