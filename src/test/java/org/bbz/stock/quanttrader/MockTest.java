//package org.bbz.stock.quanttrader;
//
//import org.junit.After;
//import org.junit.Before;
//import org.mockserver.integration.ClientAndProxy;
//import org.mockserver.integration.ClientAndServer;
//
//import static org.mockserver.integration.ClientAndProxy.startClientAndProxy;
//
//public class MockTest {
//
//    private ClientAndProxy proxy;
//    private ClientAndServer mockServer;
//
//    @Before
//    public void startProxy() {
//        mockServer = mockServer.startClientAndServer();
//        proxy = startClientAndProxy(1090);
//    }
//
//    @After
//    public void stopProxy() {
//        proxy.stop();
//        mockServer.stop();
//    }
//}
