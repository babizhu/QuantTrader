package org.bbz.stock.quanttrader.http.handler;

import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.bbz.stock.quanttrader.consts.ErrorCode;

public abstract class AbstractHandler{
    protected final EventBus eventBus;

    protected AbstractHandler( EventBus eventBus ){
        this.eventBus = eventBus;
    }

    /**
     * 根据错误id，构建相应的错误提示信息
     *
     * @param response 　 response
     * @param errId    　    错误ｉｄ
     * @return json
     */
    protected JsonObject buildErrorResponse( HttpServerResponse response, ErrorCode errId ){
        response.setStatusCode( 500 );
        return this.buildResponseJson( errId, "" );
    }

    /**
     * 统一处理错误情况
     *
     * @param errId 错误ｉｄ
     * @param msg   错误的相关参数
     * @return json
     */
    protected JsonObject buildResponseJson( ErrorCode errId, String msg ){
        return new JsonObject().put( "result", errId.toNum() ).put( "msg", msg );
    }

    /**
     * 在没有返回值的情况下，统一返回{"success":true}
     *
     * @return json
     */
    protected JsonObject buildSuccessResponse(){

        return this.buildResponseJson( ErrorCode.SUCCESS, "" );
    }

    abstract protected Router addRouter( io.vertx.ext.web.Router restAPI );


    protected void send( String address, JsonObject msg, DeliveryOptions options, RoutingContext ctx, Handler<Message<Object>> replyHandler ){
        eventBus.send( address, msg, options, reply -> {
            if( reply.succeeded() ) {
                replyHandler.handle( reply.result() );
            } else {
//                buildResponseJson(  )
                ReplyException e = (ReplyException) reply.cause();
                ctx.response().setStatusCode( 500 ).end( e.failureCode() + "" );
//                reply.cause().printStackTrace();
            }
        } );

    }
}
