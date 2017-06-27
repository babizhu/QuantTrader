package org.bbz.stock.quanttrader.model.impl.gridtrader;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by liu_k on 2017/6/26.
 */
public class StrategyGridTest{
    @Test
    public void create() throws Exception{
        List<StrategyGrid> strategyGrids = StrategyGrid.create( new BigDecimal( 10 ), "0.08", "0.15" );
        System.out.println(strategyGrids);
        assertEquals( strategyGrids.get( 0 ).getPrice(), new BigDecimal( "6.80" ) );
        assertEquals( strategyGrids.get( 0 ).getPosition(), 1,0 );

        assertEquals( strategyGrids.get( strategyGrids.size() - 1 ).getPrice(), new BigDecimal( "16.00" ) );
        assertEquals( strategyGrids.get( strategyGrids.size() - 1 ).getPosition(), 0,0 );

        assertEquals( strategyGrids.get( 3 ).getPrice(), new BigDecimal( "9.20" ) );
        assertEquals( strategyGrids.get( 3 ).getPosition(), 0.4,0.001f );
    }

}