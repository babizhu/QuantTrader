package org.bbz.stock.quanttrader.trade.stock;

import io.vertx.core.json.JsonObject;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import lombok.Data;

/**
 * Created by liukun on 2017/6/17. 交易信息记录实体类
 */
@Data
public class StockTradeRecord {

  /**
   * 当次交易是否买入
   *
   * @return true:买入
   */
  public boolean isBuy() {
    return count > 0;
  }

  /**
   * 交易日期
   */
  private LocalDateTime time;

  private String stockId;

  /**
   * 正数买进，负数卖出
   */
  private int count;

  private float price;
  /**
   * 此交易记录当前的状态，true 表示此记录为系统自动生产，尚未得到用户确认
   */
  private boolean isPending;
  /**
   * 记录每个策略特殊的买卖点信息,不可能为null
   */
  private JsonObject attachement;

  private final UUID id;

  /**
   *
   */
  StockTradeRecord(UUID id, String stockId, int count, float price, boolean isPending,
      LocalDateTime time, JsonObject attachement) {
    this.stockId = stockId;
    this.count = count;
    this.price = price;
    this.attachement = attachement == null ? new JsonObject() : attachement;
    this.isPending = true;
    this.id = id;
    this.time = time;
  }


  public static StockTradeRecord create(String stockId, int count, float price,
      JsonObject attachement) {
    final UUID id = UUID.randomUUID();
    return new StockTradeRecord(id, stockId, count, price, true, LocalDateTime.now(), attachement);
  }

  public static StockTradeRecord MapperFromDB(UUID id, String stockId, int count, float price,
      boolean isPending, int tradeTime) {

    LocalDateTime time = LocalDateTime.ofEpochSecond(tradeTime, 0, ZoneOffset.ofHours(8));

    return new StockTradeRecord(id, stockId, count, price, isPending, time, null);
  }

}
