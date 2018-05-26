package org.bbz.stock.quanttrader.vertx;//package org.bbz.stock.quanttrader.trade.stockdata;

import io.vertx.core.AsyncResult;
import io.vertx.core.Closeable;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.impl.TaskQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

/**
 * 函数编程的各种测试
 */

public class FunctionPTest {

  private void close() {
    System.out.println("FunctionPTest.close");
  }

  @Test
  public void closeFP() {
    Closeable closeable = completionHandler -> {

      close();
      completionHandler.handle(Future.succeededFuture());
    };
    closeable.close(System.out::println);
    Closeable closeHook = completionHandler -> completionHandler.handle(Future.succeededFuture());
    closeHook.close(result -> System.out.println(result.succeeded()));
  }

  @SuppressWarnings("Convert2Lambda")
  @Test
  public void closeNormal() {
    Closeable closeHook = new Closeable() {
      @Override
      public void close(Handler<AsyncResult<Void>> completionHandler) {
        AsyncResult<Void> argument = Future.succeededFuture();
        completionHandler.handle(argument);
      }
    };

    closeHook.close(new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> event) {
        System.out.println(event.succeeded());
      }
    });
  }

  @Test
  public void runFP() throws InterruptedException {
    AtomicInteger count = new AtomicInteger(0);
    TaskQueue taskQueue = new TaskQueue();
    ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    for (int i = 0; i < 100000; i++) {

      taskQueue.execute(() -> {
        String msg = "FunctionPTest.run " + Thread.currentThread().getName() + "#" + count.incrementAndGet();
        System.out.println(msg);
      }, executorService);
    }
    System.out.println("end!");
    executorService.awaitTermination(1, TimeUnit.DAYS);


  }

  @Test
  public void run() {

  }
}