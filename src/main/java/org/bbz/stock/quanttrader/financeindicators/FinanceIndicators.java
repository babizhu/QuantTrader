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
    @SuppressWarnings("SameParameterValue")
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
     * @param N 9
     * @param M1 3
     * @param M2 3
     */

    public final double[][] calcKDJ( final List<SimpleKBar> list, int N, int M1, int M2 ){
        double[] high = list.stream().mapToDouble( SimpleKBar::getHigh ).toArray();
        double[] low = list.stream().mapToDouble( SimpleKBar::getLow ).toArray();
        double[] close = list.stream().mapToDouble( SimpleKBar::getClose ).toArray();
        double outSlowK[] = new double[high.length];
        double outSlowD[] = new double[high.length];
        double outSlowJ[] = new double[high.length];
        int length = list.size();
        double[] RSV = new double[length];
        for( int i = 0; i < length; i++ ) {
            if( i >= N-1 ) {
                int start = i - (N-1);
                double highN = Double.MIN_VALUE;
                double lowN = Double.MAX_VALUE;
                while( start <= i ) {
                    if( high[start] > highN ) {
                        highN = high[start];
                    }
                    if( low[start] < lowN ) {
                        lowN = low[start];
                    }
                    start++;
                }
                RSV[i] = (close[i] - lowN) / (highN - lowN) * 100;
            } else {
                RSV[i] = 0d;
            }
        }
        for( int i = 0; i < length; i++ ) {
            if( i > 1 ) {
                outSlowK[i] = (M1 -1d) / M1 * outSlowK[i - 1] + 1d / M1 * RSV[i];
                outSlowD[i] = (M2 -1d) / M2  * outSlowD[i - 1] + 1d / M2 * outSlowK[i];
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

}

