package org.bbz.stock.quanttrader.http.handler;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

/**
 * 统一处理回送信息，各种异常信息等等
 */
@Slf4j
public class ResponseHandler implements Handler<RoutingContext>{
    @Override
    public void handle( RoutingContext ctx ){
        ctx.put("title", "Wiki home");
        final JsonObject r = ctx.get( "r" );
        log.info( "最后的流程" );
    }
}
