package org.bbz.stock.quanttrader.http.handler;

import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import org.bbz.stock.quanttrader.consts.ErrorCode;
import org.bbz.stock.quanttrader.consts.ErrorCodeException;

@Slf4j
public abstract class AbstractHandler {

  protected final EventBus eventBus;

  protected AbstractHandler(EventBus eventBus) {
    this.eventBus = eventBus;
  }

//    /**
//     * 根据错误id，构建相应的错误提示信息
//     *
//     * @param response 　 response
//     * @param errId    　    错误ｉｄ
//     * @return json
//     */
//    protected JsonObject buildErrorResponse( HttpServerResponse response, ErrorCode errId ){
//        response.setStatusCode( 500 );
//        return this.buildResponseJson( errId, "" );
//    }

  //  private JsonObject buildResponseJson(ErrorCode errId, String msg) {
//    return new JsonObject().put("result", errId.toNum()).put("msg", msg);
//  }

  /**
   * 在没有返回值的情况下，统一返回{"success":true}
   *
   * @return json
   */
//  private JsonObject buildSuccessResponse(JsonObject msg) {
//
//    return this.buildResponseJson(ErrorCode.SUCCESS, msg.toString());
//  }

  abstract protected Router addRouter(io.vertx.ext.web.Router restAPI);


  protected void send(String address, JsonObject msg, DeliveryOptions options, RoutingContext ctx,
      Handler<Message<Object>> replyHandler) {
    eventBus.send(address, msg, options, reply -> {
      if (reply.succeeded()) {
        replyHandler.handle(reply.result());
      } else {
        ctx.fail(reply.cause());
      }
    });

  }

  protected void reportError(RoutingContext ctx, ErrorCode errorCode) {
    reportError(ctx, errorCode.toNum(), "");
  }

  protected void reportError(RoutingContext ctx, ErrorCode errorCode, String msg) {
    reportError(ctx, errorCode.toNum(), msg);
  }

  private void reportError(RoutingContext ctx, int errorCode, String msg) {
    JsonObject resp = new JsonObject()
        .put("eid", errorCode)
        .put("msg", msg);

    ctx.response().setStatusCode(500).end(resp.toString());
  }

//    protected void reportSuccessMsg( RoutingContext ctx ){
//
//        ctx.response().end( buildSuccessResponse().toString() );
//    }

  /**
   * 用来处理返回值只有一个简单的errorCode和一个字符串的情况
   */
  private String buildResponseMsg(ErrorCode errorCode, String msg) {
    return "{\"result\":" + errorCode.toNum() + ",\"msg\":\"" + msg + "\"}";

  }

  protected void reportSuccessMsg(RoutingContext ctx, String msg) {
    ctx.response().end(buildResponseMsg(ErrorCode.SUCCESS, msg));
  }
//    protected void reportMsg(RoutingContext ctx, ErrorCode errorCode, String msg ){
//        if( errorCode.status() ) {
//            reportSuccessMsg( ctx );
//        }else {
//            reportError( ctx,errorCode,msg );
//        }
//    }

  /**
   * 检测客户端输入参数是否正确，不多也不少
   *
   * @param keys 需要的key
   * @param arguments 客户上传的json
   */
  protected void checkArgumentsStrict(JsonObject arguments, String... keys) {
    System.out.println();
    if (arguments.size() != keys.length) {
      throw new ErrorCodeException(ErrorCode.PARAMETER_ERROR);
    }
    checkArguments(arguments, keys);
  }

  /**
   * 检测客户端输入参数是否正确，要求keys内的项目不能少，但是其余的输入不做硬性要求
   *
   * @param keys 需要的key
   * @param arguments 客户上传的json
   */
  protected void checkArguments(JsonObject arguments, String... keys) {

    for (String key : keys) {
      if (!arguments.containsKey(key)) {
        throw new ErrorCodeException(ErrorCode.PARAMETER_ERROR, key + " is null");
      }
    }
  }

}
