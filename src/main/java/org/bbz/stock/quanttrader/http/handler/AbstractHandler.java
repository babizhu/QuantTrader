package org.bbz.stock.quanttrader.http.handler;

import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import org.bbz.stock.quanttrader.consts.ErrorCode;

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

  /**
   * 统一处理错误情况
   *
   * @param errId 错误ｉｄ
   * @param msg 错误的相关参数
   * @return json
   */
  private JsonObject buildResponseJson(ErrorCode errId, String msg) {
    return new JsonObject().put("result", errId.toNum()).put("msg", msg);
  }

  /**
   * 在没有返回值的情况下，统一返回{"success":true}
   *
   * @return json
   */
  private JsonObject buildSuccessResponse(JsonObject msg) {

    return this.buildResponseJson(ErrorCode.SUCCESS, msg.toString());
  }

  abstract protected Router addRouter(io.vertx.ext.web.Router restAPI);


    protected void send( String address, JsonObject msg, DeliveryOptions options, RoutingContext ctx,
                         Handler<Message<Object>> replyHandler ){
        eventBus.send( address, msg, options, reply -> {
            if( reply.succeeded() ) {
                replyHandler.handle( reply.result() );
            } else {
                ReplyException e = (ReplyException) reply.cause();
//                ctx.response().setStatusCode( 500 ).end( e.failureCode() + "" );
//                ctx.response().setStatusCode( 500 ).end( e.toString() );
                reportError( ctx, e.failureCode(), e.toString() );
//                reply.cause().printStackTrace();
            }
        } );

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
//        if( errorCode.isSuccess() ) {
//            reportSuccessMsg( ctx );
//        }else {
//            reportError( ctx,errorCode,msg );
//        }
//    }

}
