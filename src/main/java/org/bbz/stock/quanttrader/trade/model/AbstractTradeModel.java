package org.bbz.stock.quanttrader.trade.model;

import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liukun on 2017/7/13.
 * AbstractTradeModel
 */
public abstract class AbstractTradeModel implements ITradeModel{
    protected final Map<String, JsonObject> attachements = new HashMap<>();
//    protected String lastRunInfo;
private List<String> logs = new ArrayList<>();


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
    public String getTradeInfo(){
        return logs.toString() ;
    }
    @Override
    public void initialize(){
    }

    /**
     * 从Attachements中获取double值，如果值不存在返回null
     * @param stockId       stockId
     * @param key           key
     */
    protected Double getDoubleFromAttachements(String stockId, String key){
        final JsonObject jsonObject = attachements.get( stockId );
        if( jsonObject == null ) {
            return null;
        }
        return jsonObject.getDouble( key );
    }

    @Override
    public void refreshTradeRecords( ){

    }

    protected void addLog(String log){
        if( logs.size() > 100 ){
            logs.remove(0);
        }
        logs.add(log);
    }
}
