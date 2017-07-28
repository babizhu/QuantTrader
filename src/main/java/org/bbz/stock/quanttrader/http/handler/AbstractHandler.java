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
public abstract class AbstractHandler{
    protected final EventBus eventBus;

    protected AbstractHandler( EventBus eventBus ){
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
     * @param msg   错误的相关参数
     * @return json
     */
    private JsonObject buildResponseJson( ErrorCode errId, String msg ){
        return new JsonObject().put( "result", errId.toNum() ).put( "msg", msg );
    }

    /**
     * 在没有返回值的情况下，统一返回{"success":true}
     *
     * @return json
     */
    private JsonObject buildSuccessResponse(){

        return this.buildResponseJson( ErrorCode.SUCCESS, "" );
    }

    abstract protected Router addRouter( io.vertx.ext.web.Router restAPI );


    protected void send( String address, JsonObject msg, DeliveryOptions options, RoutingContext ctx,
                         Handler<Message<Object>> replyHandler ){
        eventBus.send( address, msg, options, reply -> {
            if( reply.succeeded() ) {
                replyHandler.handle( reply.result() );
            } else {
                ReplyException e = (ReplyException) reply.cause();
//                ctx.response().setStatusCode( 500 ).end( e.failureCode() + "" );
//                ctx.response().setStatusCode( 500 ).end( e.toString() );
                reportQueryError( ctx, e.failureCode(), e.toString() );
//                reply.cause().printStackTrace();
            }
        } );

    }
//    protected void send( String address, DeliveryOptions options, RoutingContext ctx,
//                         Handler<Message<Object>> replyHandler ){
//        eventBus.send( address, options, reply -> {
//            if( reply.succeeded() ) {
//                replyHandler.handle( reply.result() );
//            } else {
//                ReplyException e = (ReplyException) reply.cause();
//                ctx.response().setStatusCode( 500 ).end( e.failureCode() + "" );
////                reply.cause().printStackTrace();
//            }
//        } );
//    }

    protected void reportQueryError( RoutingContext ctx, ErrorCode errorCode, String msg ){
        reportQueryError( ctx, errorCode.toNum(), msg );
    }

    private void reportQueryError( RoutingContext ctx, int errorCode, String msg ){
        JsonObject resp = new JsonObject()
                .put( "eid", errorCode )
                .put( "msg", msg );

        ctx.response().setStatusCode( 500 ).end( resp.toString() );
    }

    protected void reportSuccess( RoutingContext ctx ){

        ctx.response().end( buildSuccessResponse().toString() );
    }


    protected void reportMsg(RoutingContext ctx, ErrorCode errorCode, String msg ){
        if( errorCode.isSuccess() ) {
            reportSuccess( ctx );
        }else {
            reportQueryError( ctx,errorCode,msg );
        }
    }

}
