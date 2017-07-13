package org.bbz.stock.quanttrader.trade.model;

import io.vertx.core.json.JsonObject;
import org.bbz.stock.quanttrader.trade.model.impl.wavetrade.WaveTrideModel;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by liukun on 2017/7/13.
 * AbstractTradeModel
 */
public abstract class AbstractTradeModel implements ITradeModel{
    protected final Map<String, JsonObject> attachements = new HashMap<>();
    /**
     * 设置一些额外的参数，方便操作
     *
     * @param stockId stockId
     */
    protected AbstractTradeModel setAttachement( String stockId, String key, Object value ){
        if( !attachements.containsKey( stockId ) ) {
            attachements.put( stockId, new JsonObject().put( key, value ) );
        } else {
            final JsonObject entries = attachements.get( stockId );
            entries.put( key, value );
        }
        return this;
    }
    @Override
    public void initialize(){
    }

}
