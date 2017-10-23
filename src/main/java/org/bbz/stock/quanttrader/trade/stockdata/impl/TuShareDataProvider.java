package org.bbz.stock.quanttrader.trade.stockdata.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisClient;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.bbz.stock.quanttrader.consts.KLineType;
import org.bbz.stock.quanttrader.trade.stockdata.AbstractStockDataProvider;
import org.bbz.stock.quanttrader.trade.tradehistory.SimpleKBar;
import org.bbz.stock.quanttrader.util.DateUtil;

/**
 * Created by liulaoye on 17-7-6. tushare数据提供者的实现 单例
 */
public class TuShareDataProvider extends AbstractStockDataProvider {

  private static TuShareDataProvider provider;

  public static TuShareDataProvider createShare(RedisClient redis, HttpClient httpClient) {
    if (provider == null) {
      synchronized (TuShareDataProvider.class) {  //1
        if (provider == null) {
          provider = new TuShareDataProvider(redis, httpClient);
        }
      }
    }
    return provider;
  }

//    public static TuShareDataProvider INSTANCE(){
//        return provider;
//    }

  private TuShareDataProvider(RedisClient redis, HttpClient httpClient) {
    super(redis, httpClient);
  }

  /**
   * 获取从当日起往前count个数量的k线数据
   *
   * @param stockId 股票ID
   * @param kLineType K线类型
   * @param count 数量
   * @param resultHandler 回调地狱
   */
  @Override
  public void getSimpleKBar(String stockId, KLineType kLineType, int count,
      Handler<AsyncResult<List<SimpleKBar>>> resultHandler) {
//        String uri = "/?code=" + stockId + "&&ktype=" + kLineType.toStr() + "&&share=" + share;
    getSimpleKBar(stockId, kLineType, count, null, null, resultHandler);
  }

  /**
   * tushare本身有个问题，当指定了start或者end之后，即使指定的end日期远大于当天日期，返回的kbar仍然不含当天的k线数据
   * 这里必须处理这种情况，为简便起见，这里不允许获取当日数据
   *
   * @param stockId stockId
   * @param kLineType K线类型
   * @param count 数量
   * @param start 开始日期
   * @param end 结束日期
   * @param resultHandler 回调
   */
  @Override
  public void getSimpleKBar(String stockId, KLineType kLineType, int count,
      LocalDate start,
      LocalDate end,
      Handler<AsyncResult<List<SimpleKBar>>> resultHandler) {
    String uri = "/?code=" + stockId + "&&ktype=" + kLineType.toStr() + "&&share=" + count;
    if (start != null && end != null) {
      uri += "&&start=" + DateUtil.formatDate(start) + "&&end=" + DateUtil.formatDate(end);
    }
    final HttpClientRequest request = httpClient.get(uri, resp -> {
//            String finalUri = finalUri;
      resp.exceptionHandler(exception -> {
        Future<List<SimpleKBar>> failResult = Future.failedFuture(exception);
        resultHandler.handle(failResult);
      });
      resp.bodyHandler(body -> {
        final JsonArray result = body.toJsonArray();
        if (result.size() == 0) {
          Future<List<SimpleKBar>> failResult = Future.failedFuture("股票信息不存在");
          resultHandler.handle(failResult);
          return;
        }
        final List<SimpleKBar> data = parseResponse(result);
        final SimpleKBar simpleKBar = data.get(data.size() - 1);
        if (!simpleKBar.getTime().toLocalDate().equals(LocalDate.now())) {
          Future<List<SimpleKBar>> failResult = Future.failedFuture("股票未开盘");
          resultHandler.handle(failResult);
        } else {
          Future<List<SimpleKBar>> successResult = Future.succeededFuture(data);
          resultHandler.handle(successResult);
        }
      });
    });
    request.exceptionHandler(System.out::println).end();
  }

  /**
   * 把所有的网络http请求包装成一个函数
   *
   * @param uri uri
   * @return future   future
   */
  private <R> Future<R> sendRequest(String uri, Function<Buffer, R> mapper) {



    final Future<R> future = Future.future();
    final HttpClientRequest request = httpClient.get(uri, response -> {
      response.exceptionHandler(future::fail);
      response.bodyHandler(body -> {
        final R apply = mapper.apply(body);
        if (apply == null) {
          future.fail("股票数据不存在");
        } else {
          future.complete(apply);
        }
      });
    });
    request.exceptionHandler(exception->{
      future.fail(exception);
    }).end();
    return future;
  }

//  public void getSimpleKBarEx(String stockId, KLineType kLineType, int share,
//      LocalDate start,
//      LocalDate end,
//      Handler<List<SimpleKBar>> resultHandler) {
//
//    String uri = "/?code=" + stockId + "&&ktype=" + kLineType.toStr() + "&&share=" + share;
//    if (start != null && end != null) {
//      uri += "&&start=" + DateUtil.formatDate(start) + "&&end=" + DateUtil.formatDate(end);
//    }
//    sendRequest(uri, res -> parseResponse(res.toJsonArray()))
//        .setHandler(res -> {
//          if (res.succeeded()) {
//            resultHandler.handle(res.result());
//          } else {
//            res.cause().printStackTrace();
//          }
//        });
//  }

  public void getSimpleKBarEx(String stockId, KLineType kLineType, int count,
      LocalDate start,
      LocalDate end,
      Handler<AsyncResult<List<SimpleKBar>>> resultHandler) {

    String uri = "/?code=" + stockId + "&&ktype=" + kLineType.toStr() + "&&share=" + count;
    if (start != null && end != null) {
      uri += "&&start=" + DateUtil.formatDate(start) + "&&end=" + DateUtil.formatDate(end);
    }
    sendRequest(uri, res -> parseResponse(res.toJsonArray()))
        .setHandler(res -> {
          if (res.succeeded()) {
            final List<SimpleKBar> result = res.result();
            if (result.size() == 0) {
              resultHandler.handle(Future.failedFuture("股票信息不存在"));
            } else {
              final SimpleKBar simpleKBar = result.get(result.size() - 1);
              if (!simpleKBar.getTime().toLocalDate().equals(LocalDate.now())) {
                Future<List<SimpleKBar>> failResult = Future.failedFuture("股票未开盘");
                resultHandler.handle(failResult);
              } else {
                resultHandler.handle(res);
              }

            }
          } else {
            resultHandler.handle(res);
          }
        });
  }

  private List<SimpleKBar> parseResponse(JsonArray response) {
    if (response.size() == 0) {
      return null;
    }
    List<SimpleKBar> data = new ArrayList<>();
    for (Object o : response) {
      final JsonObject jo = (JsonObject) o;
      data.add(new SimpleKBar(DateUtil.parse(jo.getString("date")), jo.getDouble("open"),
          jo.getDouble("high")
          , jo.getDouble("low"), jo.getDouble("close")
          , 100));
    }
    return data;

  }

  /**
   * 专用于getCurrentKbar的转换函数 getCurrentKbar方法从http得来的数据即使是数字也包含有引号，所以需要特殊处理
   *
   * @param response http响应
   * @return SimpleKBar
   */
  private SimpleKBar parseResponse(JsonObject response) {

    return new SimpleKBar(DateUtil.parse(response.getString("date")),
        Double.parseDouble(response.getString("open")),
        Double.parseDouble(response.getString("high")),
        Double.parseDouble(response.getString("low")),
        Double.parseDouble(response.getString("price")),
        Integer.parseInt(response.getString("volume")));
  }

  @Override
  public void getCurrentKbar(String stockId, Handler<SimpleKBar> resultHandler) {
    String uri = "/getCurrentPrice/" + stockId;
    final Future<SimpleKBar> future = sendRequest(uri,
        res -> parseResponse(res.toJsonArray().getJsonObject(0)));
    future.setHandler(res -> {
      if (res.succeeded()) {
        resultHandler.handle(res.result());
      } else {
        res.cause().printStackTrace();
      }
    });
//        resultHandler.handle( future );

//        future.setHandler( res -> {
//            if(res.succeeded() ){
//                System.out.println( res.result() );
//            }else {
//                res.cause().printStackTrace();
//            }
//        } );
//        final HttpClientRequest request = httpClient.get( uri, resp -> {
//            resp.exceptionHandler( exception -> {
//                Future<SimpleKBar> failResult = Future.failedFuture( exception );
//                resultHandler.handle( failResult );
//            } );
//            resp.bodyHandler( body -> {
//                final JsonObject jo = body.toJsonObject();
//                final SimpleKBar simpleKBar = new SimpleKBar( DateUtil.parse( jo.getString( "date" ) ), jo.getDouble( "open" ), jo.getDouble( "high" )
//                        , jo.getDouble( "low" ), jo.getDouble( "price" )
//                        , jo.getInteger( "volume" ) );
//
//                Future<SimpleKBar> successResult = Future.succeededFuture( simpleKBar );
//                resultHandler.handle( successResult );
//            } );
//        } );
//        request.exceptionHandler( System.out::println ).end();
  }

//    private void sendRequest( String url)

}