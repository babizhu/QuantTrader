package org.bbz.stock.quanttrader.trade.stockdata.impl;

import static org.bbz.stock.quanttrader.trade.stockdata.impl.TuShareDataProvider.createShare;

import com.google.common.collect.Lists;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestOptions;
import io.vertx.ext.unit.TestSuite;
import io.vertx.ext.unit.report.ReportOptions;
import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.bbz.stock.quanttrader.consts.KLineType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TuShareDataProviderTest {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  private TuShareDataProvider createDataProvider() {
    final HttpClientOptions httpClientOptions = new HttpClientOptions();

    Vertx vertx = Vertx.vertx();
    String host = "localhost";

    httpClientOptions.setDefaultPort(8888).setDefaultHost(host).setConnectTimeout(4000)
        .setKeepAlive(true);
    final TuShareDataProvider dataProvider = createShare(null,
        vertx.createHttpClient(httpClientOptions));
    return dataProvider;
  }

  @Test
  public void getSimpleKBarEx() throws Exception {
    TestSuite suite = TestSuite.create("the_test_suite");
    suite.test("my_test_case", context -> {
      Async async = context.async();

      createDataProvider().getSimpleKBarEx("600740", KLineType.DAY, 100, null, null, res -> {
        if (res.succeeded()) {
          System.out.println(res.result());
        } else {
          res.cause().printStackTrace();
        }
        async.complete();
      });
    });
    suite.run(new TestOptions().addReporter(new ReportOptions().setTo("console")));
//    suite.
    try {
      Thread.sleep(10000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void getCurrentKbar() throws Exception {
    final TuShareDataProvider dataProvider = createDataProvider();
    dataProvider.getCurrentKbar("600740", System.out::println);



    try {
      Thread.sleep(10000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testFunction() {
    Function<String, JsonObject> f1 = s -> new JsonObject().put("arg", s);
    final Function<String, ArrayList<String>> arg = f1.andThen(json -> json.getString("arg"))
        .andThen(s -> s.split(","))
        .andThen(arr -> Lists.newArrayList(arr));

    final ArrayList<String> apply = arg.apply("a,b,c,d");
    System.out.println(apply);


    Function<String, JsonObject> f2 = s -> new JsonObject().put("arg", s);
    final JsonObject apply1 = f2.compose(str -> str.getClass().getName()).apply("a.b.b.");
    System.out.println(apply1);

    Function<String, String > f3 = s ->  s.getClass().getName();
    final String arg1 = f3.andThen(s -> new JsonObject().put("arg", s))
        .andThen(json -> json.toString()).apply("a,b,cds");
    System.out.println(arg1);

    BiFunction<String,Integer,Double> bf1 = (s, integer) -> s.length() * integer + 343.01d;
    final Double abcd = bf1.apply("abcd", 2);
    Function<Double,String > f4 = s-> s.toString()+"abcd";

    System.out.println(f4.apply(bf1.apply("abcd",10)));
  }


}