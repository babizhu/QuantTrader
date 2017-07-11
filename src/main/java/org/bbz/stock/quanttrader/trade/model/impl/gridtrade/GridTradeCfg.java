package org.bbz.stock.quanttrader.trade.model.impl.gridtrade;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by liukun on 2017/6/17.
 * 网格交易法的配置文件
 */
@Data
public class GridTradeCfg{

    private final List<StrategyGrid> grids;
    /**
     * 底仓价格
     */
    private final BigDecimal basePrice;


    /**
     * 给出一个价格，计算当前应该运用哪个格子进行操作
     *
     * @param price 当前价格
     * @return 相应grid
     */
    StrategyGrid calcGrid( BigDecimal price ){

        int index;
        for( index = 0; index < grids.size(); index++ ) {
            StrategyGrid grid = grids.get( index );
            int compareRes = price.compareTo( grid.getPrice() );

            if( compareRes == 0 ) {
                return grid;
            } else if( compareRes == -1 ) {
                break;
            }
        }

        boolean needBuy = price.compareTo( basePrice ) == -1;
        if( needBuy ) {
            return grids.get( index );
        } else {
            return grids.get( index - 1 );
        }
    }

}
