package org.bbz.stock.quanttrader.financeindicators;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by liukun on 2017/7/3.
 * FinanceIndicatorsTest
 */
public class FinanceIndicatorsTest{


    private List<Double> closes600848 = new ArrayList<>(  );
    private List<Double> closes002769 = new ArrayList<>(  );
    @Before
    public void prepareData(){
        double[] data600848 = {
                21.98,
                21.56,
                21.74,
                21.67,
                22.2,
                23.26,
                23.34,
                25.93,
                23.57,
                24.08,
                22.5,
                20.8,
                20.21,
                19.3,
                19.12,
                18.14,
                19.07,
                19.0,
                19.3,
                18.55,
                18.69,
                18.18,
                17.94,
                17.91,
                17.77,
                16.99,
                16.82,
                17.41,
                17.74,
                17.85,
                17.8,
                17.93,
                17.7,
                17.51,
                17.6,
                17.92,
                18.32,
                18.72,
                18.52,
                18.64,
                19.11,
                19.35,
                19.38,
                19.45,
                18.74,
                18.8,
                18.75,
                18.72,
                19.63,
                19.5,
                20.23,
                21.47,
                20.84,
                20.66,
                21.43,
                21.41,
                20.66,
                20.73,
                21.91,
                20.81,
                20.99,
                19.68,
                19.91,
                19.67,
                19.7,
                19.76,
                19.96,
                19.5,
                19.89,
                20.34,
                20.48,
                20.42,
                20.57,
                20.58,
                20.89,
                20.78,
                21.11,
                20.93,
                21.1,
                21.41,
                21.98,
                21.94,
                21.79,
                22.08,
                21.63,
                21.53,
                21.93,
                22.22,
                20.88,
                21.33,
                21.59,
                21.59,
                22.35,
                21.78,
                21.23,
                20.79,
                20.45,
                20.67,
                20.65,
                20.89

        };

        closes600848 = new ArrayList<>();
        for( int i = data600848.length; i > 0; i-- ) {
            closes600848.add( data600848[i - 1] );
        }
        double[] data002769 = {
                16.72,
                15.2,
                15.13,
                14.97,
                15.34,
                15.21,
                15.15,
                15.5,
                15.88,
                15.75,
                15.8,
                15.93,
                16.07,
                15.78,
                15.91,
                15.37,
                15.79,
                16.0,
                16.17,
                16.12,
                15.22,
                13.84,
                13.25,
                14.08,
                14.08,
                14.03,
                13.9,
                13.93,
                14.74,
                15.48,
                15.73,
                15.51,
                15.62,
                14.88,
                14.81,
                14.91,
                15.32,
                16.12,
                16.08,
                16.92,
                16.91,
                17.02,
                17.1,
                17.21,
                17.16,
                17.1,
                17.03,
                17.05,
                18.0,
                18.2,
                18.59,
                18.77,
                18.75,
                19.06,
                19.4,
                19.4,
                19.5,
                19.19,
                19.74,
                19.95,
                20.07,
                19.4,
                19.31,
                19.89,
                20.14,
                19.84,
                19.75,
                19.86,
                20.22,
                20.7,
                20.54,
                20.76,
                20.9,
                20.72,
                21.12,
                21.33,
                21.2,
                21.2,
                21.66,
                21.88,
                22.15,
                21.46,
                21.7,
                21.97,
                22.59,
                22.45,
                22.15,
                21.27,
                21.52,
                21.54,
                21.16,
                20.78,
                21.4,
                20.54,
                20.76,
                20.92,
                20.78,
                21.45,
                21.27,
                19.34,
        };
        closes002769 = new ArrayList<>();
        for( int i = data002769.length; i > 0; i-- ) {
            closes002769.add( data002769[i - 1] );
        }

//        System.out.println( closes600848 );
//        System.out.println( closes600848.size());
    }
    @Test
    public void getEXPMA() throws Exception{

//        List<Double> data = closes002769;
        List<Double> data = closes600848;
        Double expma = FinanceIndicators.INSTANCE.getEXPMA(data , 12 );
        System.out.println( expma );
        expma = FinanceIndicators.INSTANCE.getEXPMA( data, 5 );
        System.out.println( expma );
    }

    @Test
    public void getDiff() throws Exception{
        List<Double> data = closes600848;
        double diff = FinanceIndicators.INSTANCE.getDiff( data );
        System.out.println(diff);
    }

    @Test
    public void getMACD() throws Exception{
        System.out.println( FinanceIndicators.INSTANCE.getMACD( closes600848,5,21,8 ));
        System.out.println( FinanceIndicators.INSTANCE.getMACD( closes600848,5,8,21 ));
        System.out.println( FinanceIndicators.INSTANCE.getMACD( closes600848,21,8,5 ));
        System.out.println( FinanceIndicators.INSTANCE.getMACD( closes600848,21,5,8 ));
        System.out.println( FinanceIndicators.INSTANCE.getMACD( closes600848,8,5,21 ));
        System.out.println( FinanceIndicators.INSTANCE.getMACD( closes600848,8,21,5 ));
    }

}