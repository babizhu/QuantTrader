package org.bbz.stock.quanttrader.trade.model.impl.wavetrade;

import org.bbz.stock.quanttrader.consts.KLineType;

/**
 * Created by liulaoye on 17-7-12.
 * 保存各种回调的中间结果
 */

public class CheckResult{

    private final int resultCode;

    public int getResultCode(){
        return resultCode;
    }

    public KLineType getkLineType(){
        return kLineType;
    }

    private final KLineType kLineType;

    private CheckResult( int resultCode, KLineType kLineType ){
        this.resultCode = resultCode;
        this.kLineType = kLineType;
    }

    /**
     *
     * @param resultCode
     * 0：成功
     * 1：60分钟K线未形成上摆
     */
    @SuppressWarnings("SameParameterValue")
    static CheckResult createResult( int resultCode ){
        return new CheckResult( resultCode, null );
    }

    static CheckResult createResult( KLineType kLineType ){
        return new CheckResult( 0, kLineType );
    }
}
