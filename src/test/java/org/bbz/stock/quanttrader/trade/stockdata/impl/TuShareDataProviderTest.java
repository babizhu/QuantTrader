package org.bbz.stock.quanttrader.trade.stockdata.impl;

import static org.bbz.stock.quanttrader.trade.stockdata.impl.TuShareDataProvider.createShare;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestOptions;
import io.vertx.ext.unit.TestSuite;
import io.vertx.ext.unit.report.ReportOptions;
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
        if(res.succeeded() ){
          System.out.println(res.result());
        }else {
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

  }

}