package org.bbz.stock.quanttrader.http.handler.user;

import com.google.common.base.Strings;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import org.bbz.stock.quanttrader.consts.EventBusAddress;
import org.bbz.stock.quanttrader.consts.EventBusCommand;
import org.bbz.stock.quanttrader.consts.JsonConsts;
import org.bbz.stock.quanttrader.http.handler.AbstractHandler;
import org.bbz.stock.quanttrader.http.handler.auth.anno.RequirePermissions;
import org.bbz.stock.quanttrader.http.utils.CustomHashStrategy;

/**
 * 负责user相关接口 2017-07-24 16:20 author:liulaoye
 */
@Slf4j
public class UserHandler extends AbstractHandler {

//  private final JWTAuth jwtAuthProvider;

  public UserHandler(EventBus eventBus) {
    super(eventBus);
//    this.jwtAuthProvider = jwtAuthProvider;

  }

  @Override
  public Router addRouter(Router restAPI) {
    restAPI.route("/save").handler(this::save);
    restAPI.route("/del").handler(this::del);
    restAPI.route("/query").handler(this::query);

//        restAPI.route( "/login" ).handler( this::login );

    return restAPI;
  }

  @RequirePermissions("sys:user:query")
  private void query(RoutingContext ctx) {
    JsonObject condition = new JsonObject();
    HttpServerRequest request = ctx.request();
    String name = request.getParam(JsonConsts.USER_NAME);
    if (!Strings.isNullOrEmpty(name)) {
      condition.put(JsonConsts.USER_NAME, name);
    }
    String id = request.getParam("id");
    if (!Strings.isNullOrEmpty(id)) {
      condition.put(JsonConsts.MONGO_DB_ID, id);
    }

    DeliveryOptions options = new DeliveryOptions()
        .addHeader("action", EventBusCommand.DB_USER_QUERY.name());

    send(EventBusAddress.DB_ADDR, condition, options, ctx, reply ->
        ctx.response().end(reply.body().toString()));
  }

  private void del(RoutingContext ctx) {
    JsonObject delJson = ctx.getBodyAsJson();
    checkArgumentsStrict(delJson,JsonConsts.MONGO_DB_ID);
    DeliveryOptions options = new DeliveryOptions()
        .addHeader("action", EventBusCommand.DB_USER_DELETE.name());
    send(EventBusAddress.DB_ADDR, delJson, options, ctx, reply ->
        ctx.response().end());
  }

  /**
   * 添加或者修改用户信息
   */
  private void save(RoutingContext ctx) {
    JsonObject userJson = ctx.getBodyAsJson();
    String postId = userJson.getString(JsonConsts.MONGO_DB_ID);

    boolean isCreate = postId.equals("-1");
    if (isCreate) {
      create(ctx, userJson);
    } else {
      update(ctx, userJson);
    }
  }

  private void update(RoutingContext ctx, JsonObject updateJson) {

    checkArguments(updateJson, "email", "phone", JsonConsts.MONGO_DB_ID, "roles");
    DeliveryOptions options = new DeliveryOptions()
        .addHeader("action", EventBusCommand.DB_USER_UPDATE.name());
    send(EventBusAddress.DB_ADDR, updateJson, options, ctx, reply -> {
      final JsonObject result = (JsonObject) reply.body();
      log.info(result.toString());
      ctx.response().end();
    });

  }

  private void create(RoutingContext ctx, JsonObject userJson) {
    checkArgumentsStrict(userJson, "email", "phone","address",
        JsonConsts.MONGO_DB_ID, "roles", "username", "password");
    final String salt = CustomHashStrategy.generateSalt();
    userJson.put(JsonConsts.USER_SALT, salt);
    String cryptPassword = CustomHashStrategy.INSTANCE
        .cryptPassword(userJson.getString(JsonConsts.USER_PASSWORD), salt);

    userJson.put(JsonConsts.USER_PASSWORD, cryptPassword);
    userJson.remove(JsonConsts.MONGO_DB_ID);//去掉_id，以便让mongodb自动生成

    DeliveryOptions options = new DeliveryOptions()
        .addHeader("action", EventBusCommand.DB_USER_CREATE.name());
    send(EventBusAddress.DB_ADDR, userJson, options, ctx, reply -> {
      final String id = (String) reply.body();
      ctx.response().end(new JsonObject().put(JsonConsts.MONGO_DB_ID,id).toString());
    });
  }
}
