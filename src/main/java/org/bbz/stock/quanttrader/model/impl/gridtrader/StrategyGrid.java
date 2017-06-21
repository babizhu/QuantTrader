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
    private final float               position;


    public static List<StrategyGrid> create( BigDecimal basePrice ){

        List<StrategyGrid> ret = new ArrayList<>(  );
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
}
