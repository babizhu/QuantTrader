package org.bbz.stock.quanttrader.test;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestVerticle extends AbstractVerticle {



  private static int count = 0;
  private UUID uuid = UUID.randomUUID();

  public TestVerticle() {

    super();
    count++;
  }
  @Override
  public void start() throws Exception {
    super.start();

//    System.out.println(share);
//    System.out.println(uuid);
    System.out.println(this+"TestVerticle.start");
  }

  @Override
  public void init(Vertx vertx, Context context) {
    super.init(vertx, context);
    System.out.println(this+"TestVerticle.init");
  }

  public static void main(String[] args) {
    VertxOptions vertxOptions = new VertxOptions();
    Vertx vertx = Vertx.vertx(vertxOptions);

    DeploymentOptions options = new DeploymentOptions();
    options.setInstances(5);
    vertx.deployVerticle(TestVerticle.class.getName(), options, res -> {
      if (res.succeeded()) {
        log.info(" server started ");
      } else {
        res.cause().printStackTrace();
      }
    });
  }
}
