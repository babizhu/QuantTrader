package org.bbz.stock.quanttrader.trade.model;

import com.google.common.collect.Lists;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import org.bbz.stock.quanttrader.consts.JsonConsts;
import org.bbz.stock.quanttrader.trade.core.QuantTradeContext;
import org.bbz.stock.quanttrader.trade.stock.StockTradeRecord;
import org.bbz.stock.quanttrader.util.DateUtil;

/**
 * Created by liukun on 2017/7/13. AbstractTradeModel
 */
@Getter
public abstract class AbstractTradeModel implements ITradeModel {

  protected final Map<String, JsonObject> attachements = new HashMap<>();
  protected final QuantTradeContext ctx;
  protected final String id;
  protected List<String> logs = new ArrayList<>();
  protected final String desc;
  protected final String name;
  protected int status;
  protected LocalDateTime startTime;
  private final Set<String> stockPool;


  public AbstractTradeModel(QuantTradeContext ctx, String name, String id, String desc,
      int status, Set<String> stockPool) {
    this.ctx = ctx;
    this.name = name;
    this.id = id;
    this.status = status;
    this.desc = desc;
    this.stockPool = stockPool;
  }


  /**
   * 设置一些额外的参数，方便操作
   *
   * @param stockId stockId
   */
  protected AbstractTradeModel setAttachement(String stockId, String key, Object value) {
    if (!attachements.containsKey(stockId)) {
      attachements.put(stockId, new JsonObject().put(key, value));
    } else {
      final JsonObject entries = attachements.get(stockId);
      entries.put(key, value);
    }
    return this;
  }

  @Override
  public String getTradeInfo() {
    StringBuilder ret = new StringBuilder();
    for (String s : Lists.reverse(logs)) {
      ret.append(s);
    }
    return ret.toString();
  }

  @Override
  public void initialize() {
  }

  /**
   * 从Attachements中获取double值，如果值不存在返回null
   *
   * @param stockId stockId
   * @param key key
   */
  protected Double getDoubleFromAttachements(String stockId, String key) {
    final JsonObject jsonObject = attachements.get(stockId);
    if (jsonObject == null) {
      return null;
    }
    return jsonObject.getDouble(key);
  }

  @Override
  public void refreshTradeRecords() {

  }

  @Override
  public QuantTradeContext getQuantTradeContext() {
    return ctx;
  }


  protected void addLog(String log) {
    if (logs.size() > 100) {
      logs.remove(0);
    }
    logs.add(log);
  }

  public JsonObject toJson() {
    JsonArray tradeRecord = new JsonArray();
    for (StockTradeRecord stockTradeRecord : ctx.getTradeRecords()) {
      final JsonObject jsonObject = JsonObject.mapFrom(stockTradeRecord);
      jsonObject.put("time", DateUtil.formatDate(stockTradeRecord.getTime()));
      tradeRecord.add(jsonObject);
    }

    return new JsonObject()
        .put("tradeDetail",
            new JsonObject()
                .put(JsonConsts.TRADE_RECORDS, tradeRecord)
                .put(JsonConsts.MODEL_NAME_KEY, getName())
                .put(JsonConsts.MONGO_DB_ID, getId())
                .put(JsonConsts.MODEL_DESC_KEY, getDesc())
                .put(JsonConsts.MODEL_STATUS_KEY, getStatus()));

  }
}
