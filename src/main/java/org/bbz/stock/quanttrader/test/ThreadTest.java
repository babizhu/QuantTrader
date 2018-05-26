package org.bbz.stock.quanttrader.test;

import io.vertx.core.impl.TaskQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadTest {

  static void test1() {
    AtomicInteger count = new AtomicInteger(0);
    ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    for (int i = 0; i < 700000; i++) {
      executorService
          .execute(() -> System.out.println(Thread.currentThread().getName() + "|" + count.incrementAndGet()));
    }
    System.out.println("end");
    executorService.shutdown();
  }

  static void taskQueue() {
    AtomicInteger count = new AtomicInteger(0);
    TaskQueue taskQueue = new TaskQueue();
    ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
//    for (int i = 0; i < 1000000; i++) {

    taskQueue.execute(() -> {
      String msg = "FunctionPTest.run " + Thread.currentThread().getName() + "#" + count.incrementAndGet();
      try {
        Thread.sleep(10000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      System.out.println(msg);
    }, executorService);

    taskQueue.execute(() -> {
      String msg = "newSingleThreadExecutor " + Thread.currentThread().getName() + "#" + count.incrementAndGet();
      System.out.println(msg);
    }, Executors.newFixedThreadPool(2));
    taskQueue.execute(() -> {
      String msg = "FunctionPTest.run " + Thread.currentThread().getName() + "#" + count.incrementAndGet();
      System.out.println(msg);
    }, executorService);
//  }
    System.out.println("end!");
//    executorService.shutdown();
  }

  public static void main(String[] args) {
    taskQueue();
  }
}
