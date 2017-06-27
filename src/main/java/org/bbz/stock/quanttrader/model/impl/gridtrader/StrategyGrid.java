package org.bbz.stock.quanttrader.model.impl.gridtrader;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liu_k on 2017/6/21.
 * 策略格子
 */
@Data
public class StrategyGrid{
    /**
     * 价格
     */
    private final BigDecimal price;
    /**
     * 仓位
     */
    private final float position;


    public static List<StrategyGrid> create( BigDecimal basePrice ){

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
     * @return
     */
    public static List<StrategyGrid> create( BigDecimal basePrice, String buyStep, String sellStep ){
        BigDecimal buyUnit =  new BigDecimal( buyStep );
        BigDecimal sellUnit = new BigDecimal( sellStep );

//
        List<StrategyGrid> ret = new ArrayList<>();

        ret.add( new StrategyGrid( basePrice.multiply( BigDecimal.ONE.subtract( buyUnit.multiply( BigDecimal.valueOf( 4 ) ) ) ), 1f ) );
        ret.add( new StrategyGrid( basePrice.multiply( BigDecimal.ONE.subtract( buyUnit.multiply( BigDecimal.valueOf( 3 ) ) ) ), 0.9f ) );
        ret.add( new StrategyGrid( basePrice.multiply( BigDecimal.ONE.subtract( buyUnit.multiply( BigDecimal.valueOf( 2 ) ) ) ), 0.7f ) );
        ret.add( new StrategyGrid( basePrice.multiply( BigDecimal.ONE.subtract( buyUnit ) ), 0.4f ) );
        ret.add( new StrategyGrid( basePrice.multiply( BigDecimal.ONE.add( sellUnit ) ), 0.6f ) );
        ret.add( new StrategyGrid( basePrice.multiply( BigDecimal.ONE.add( sellUnit.multiply( BigDecimal.valueOf( 2 ) ) ) ), 0.3f ) );
        ret.add( new StrategyGrid( basePrice.multiply( BigDecimal.ONE.add( sellUnit.multiply( BigDecimal.valueOf( 3 ) ) ) ), 0.1f ) );
        ret.add( new StrategyGrid( basePrice.multiply( BigDecimal.ONE.add( sellUnit.multiply( BigDecimal.valueOf( 4 ) ) ) ), 0f ) );
        return ret;
    }
}
