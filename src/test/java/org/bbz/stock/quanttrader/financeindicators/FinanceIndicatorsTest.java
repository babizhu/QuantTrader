package org.bbz.stock.quanttrader.financeindicators;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liulaoye on 17-7-3.
 * FinanceIndicatorsTest
 */
public class FinanceIndicatorsTest{
    @Test
    public void getEXPMA(){
        List<Double> list = new ArrayList<>();
        for( int i = 0; i < 10; i++ ) {
            list.add( (double) i );
        }
        final Double expma = FinanceIndicators.INSTANCE.getEXPMA( list, 10 );
        System.out.println( expma );
    }

}