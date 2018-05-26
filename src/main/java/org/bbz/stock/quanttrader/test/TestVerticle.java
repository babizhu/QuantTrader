package org.bbz.stock.quanttrader.test;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.impl.ContextTask;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * @author liulaoye
 */
@Slf4j
public class TestVerticle extends AbstractVerticle {


  static class Student {

    String name;
    int age;

    public Student(String a, int i) {
      this.name = a;
      this.age = i;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Student student = (Student) o;
      return age == student.age &&
          Objects.equals(name, student.name);
    }

    @Override
    public int hashCode() {

      return Objects.hash(name, age);
    }
  }

  private static int count = 0;
  private UUID uuid = UUID.randomUUID();

  public TestVerticle() {

    super();
    count++;
  }

  @Override
  public void start() throws Exception {
    super.start();
    ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
    threadGroup.list();
    System.out.println(threadGroup.activeCount());

    System.out.println(threadGroup.getName());

    HttpClient httpClient = vertx.createHttpClient();

    HttpClientRequest httpRequest = httpClient
        .get("www.sina.com", "/", resp -> log.debug(resp.statusCode() + ""));
    httpRequest.connectionHandler(con -> {
      System.out.println(con.getWindowSize());
      System.out.println(con);
    });
    httpRequest.exceptionHandler(exception -> exception.printStackTrace());
    httpRequest.end();
//    System.out.println(share);
    wrapTask(() -> System.out.println(this)).run();
  }

  Runnable wrapTask(ContextTask task) {
    if (task != null) {

      return () -> {
        try {
          task.run();
        } catch (Exception e) {
          e.printStackTrace();
        }
      };
    } else {
      throw new RuntimeException();
    }
  }


  @Override
  public void init(Vertx vertx, Context context) {
    super.init(vertx, context);
  }

  public static void main(String[] args) {

    VertxOptions vertxOptions = new VertxOptions();
    Vertx vertx = Vertx.vertx(vertxOptions);

    DeploymentOptions options = new DeploymentOptions();
    options.setInstances(1);
    vertx.deployVerticle(TestVerticle.class.getName(), options, res -> {
      if (res.succeeded()) {
        log.info(" server started ");
      } else {
        res.cause().printStackTrace();
      }
    });

  }
}
