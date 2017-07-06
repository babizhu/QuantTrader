package org.bbz.stock.quanttrader.model.impl.gridtrade;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liu_k on 2017/6/21.
 * 策略格子
 */
@Data
class StrategyGrid{
    /**
     * 价格
     */
    private final BigDecimal price;
    /**
     * 仓位
     */
    private final float position;


    static List<StrategyGrid> create( BigDecimal basePrice ){

        List<StrategyGrid> ret = new ArrayList<>();
        ret.add( new StrategyGrid( basePrice.multiply( BigDecimal.valueOf( 0.88 ) ), 1f ) );
        ret.add( new StrategyGrid( basePrice.multiply( BigDecimal.valueOf( 0.91 ) ), 0.9f ) );
        ret.add( new StrategyGrid( basePrice.multiply( BigDecimal.valueOf( 0.94 ) ), 0.7f ) );
        ret.add( new StrategyGrid( basePrice.multiply( BigDecimal.valueOf( 0.97 ) ), 0.4f ) );
        ret.add( new StrategyGrid( basePrice.multiply( BigDecimal.valueOf( 1.05 ) ), 0.6f ) );
        ret.add( new StrategyGrid( basePrice.multiply( BigDecimal.valueOf( 1.1 ) ), 0.3f ) );
        ret.add( new StrategyGrid( basePrice.multiply( BigDecimal.valueOf( 1.15 ) ), 0.1f ) );
        ret.add( new StrategyGrid( basePrice.multiply( BigDecimal.valueOf( 1.2 ) ), 0f ) );
        return ret;
    }

    /**
     * @param basePrice 基础价格
     * @param buyStep   步进（买）：如果股票跌多少，则买入,例如0.035,表示股价相对基准价格跌3.5%则买入
     * @param sellStep  步进（卖）：如果股票涨多少，则卖出,例如0.035,表示股价相对基准价格涨3.5%则卖出
     */
    static List<StrategyGrid> create( BigDecimal basePrice, String buyStep, String sellStep ){
        BigDecimal buyUnit = new BigDecimal( buyStep );
        BigDecimal sellUnit = new BigDecimal( sellStep );
        final BigDecimal ONE = BigDecimal.valueOf( 1 );
        final BigDecimal TWO = BigDecimal.valueOf( 2 );
        final BigDecimal THREE = BigDecimal.valueOf( 3 );
        final BigDecimal FOUR = BigDecimal.valueOf( 4 );
//
        List<StrategyGrid> ret = new ArrayList<>();

        ret.add( new StrategyGrid( basePrice.multiply( ONE.subtract( buyUnit.multiply( FOUR ) ) ), 1f ) );
        ret.add( new StrategyGrid( basePrice.multiply( ONE.subtract( buyUnit.multiply( THREE ) ) ), 0.9f ) );
        ret.add( new StrategyGrid( basePrice.multiply( ONE.subtract( buyUnit.multiply( TWO ) ) ), 0.7f ) );
        ret.add( new StrategyGrid( basePrice.multiply( ONE.subtract( buyUnit ) ), 0.4f ) );
        ret.add( new StrategyGrid( basePrice.multiply( ONE.add( sellUnit ) ), 0.6f ) );
        ret.add( new StrategyGrid( basePrice.multiply( ONE.add( sellUnit.multiply( TWO ) ) ), 0.3f ) );
        ret.add( new StrategyGrid( basePrice.multiply( ONE.add( sellUnit.multiply( THREE ) ) ), 0.1f ) );
        ret.add( new StrategyGrid( basePrice.multiply( ONE.add( sellUnit.multiply( FOUR ) ) ), 0f ) );
        return ret;
    }
}
