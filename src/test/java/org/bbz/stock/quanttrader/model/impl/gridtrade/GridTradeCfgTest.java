package org.bbz.stock.quanttrader.model.impl.gridtrade;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by liu_k on 2017/6/21.
 * test
 */
public class GridTradeCfgTest{
    @Test
    public void calcGrid() throws Exception{
        BigDecimal basePrice = BigDecimal.valueOf( 10 );
        List<StrategyGrid> strategyGrids = StrategyGrid.create( basePrice );
        strategyGrids.forEach( System.out::println );
        GridTradeCfg tradeCfg = new GridTradeCfg( strategyGrids, basePrice );

        StrategyGrid grid = tradeCfg.calcGrid( BigDecimal.valueOf( 8.8 ) );//list内最小值
        assertEquals( 1.0f, grid.getPosition(), 0 );

        grid = tradeCfg.calcGrid( BigDecimal.valueOf( 12 ) );//list内最大值
        assertEquals( 0f, grid.getPosition(), 0 );

        grid = tradeCfg.calcGrid( BigDecimal.valueOf( 11 ) );//list内准确对应值
        assertEquals( 0.3f, grid.getPosition(), 0 );

        grid = tradeCfg.calcGrid( BigDecimal.valueOf( 15 ) );//最大值
        assertEquals( 0f, grid.getPosition(), 0 );

        grid = tradeCfg.calcGrid( BigDecimal.valueOf( 7.8 ) );//最小值
        assertEquals( 1.0f, grid.getPosition(), 0 );

        grid = tradeCfg.calcGrid( BigDecimal.valueOf( 9.2 ) );//买入中间值
        assertEquals( 0.7f, grid.getPosition(), 0 );

        grid = tradeCfg.calcGrid( BigDecimal.valueOf( 11.6 ) );//卖出中间值
        assertEquals( 0.1f, grid.getPosition(), 0 );
//
    }

}