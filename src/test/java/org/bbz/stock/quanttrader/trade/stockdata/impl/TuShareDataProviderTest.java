//package org.bbz.stock.quanttrader.trade.stockdata.impl;
//import static org.mockito.Mockito.*;
//import io.vertx.core.Vertx;
//import io.vertx.core.http.HttpClientOptions;
//import io.vertx.redis.RedisClient;
//import org.bbz.stock.quanttrader.consts.KLineType;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.time.LocalDate;
//
///**
// * Created by liulaoye on 17-7-17.
// * TuShareDataProviderTest
// */
//public class TuShareDataProviderTest{
//    private TuShareDataProvider provider;
//    @Before
//    public void init(){
//        //Create mock object of BookDAL
//        provider = mock(TuShareDataProvider.class);
//
//        //Create few instances of Book class.
////        book1 = new Book("8131721019","Compilers Principles",
////                Arrays.asList("D. Jeffrey Ulman","Ravi Sethi", "Alfred V. Aho", "Monica S. Lam"),
////                "Pearson Education Singapore Pte Ltd", 2008,1009,"BOOK_IMAGE");
////
////        book2 = new Book("9788183331630","Let Us C 13th Edition",
////                Arrays.asList("Yashavant Kanetkar"),"BPB PUBLICATIONS", 2012,675,"BOOK_IMAGE");
////
////        //Stubbing the methods of mocked BookDAL with mocked data.
////        when(mockedBookDAL.getAllBooks()).thenReturn(Arrays.asList(book1, book2));
////        when(mockedBookDAL.getBook("8131721019")).thenReturn(book1);
////        when(mockedBookDAL.addBook(book1)).thenReturn(book1.getIsbn());
////        when(mockedBookDAL.updateBook(book1)).thenReturn(book1.getIsbn());
////        when( provider.getCurrentKbar( "600090",null ) );
////
//        final Vertx vertx = Vertx.vertx();
//        final RedisClient redisClient = RedisClient.MapperFromDB( vertx );
//        final HttpClientOptions httpClientOptions = new HttpClientOptions();
//        httpClientOptions.setDefaultPort( 8888 ).setDefaultHost( "localhost" ).setConnectTimeout( 4000 ).setKeepAlive( true );
////        provider = TuShareDataProvider.createShare( redisClient, vertx.createHttpClient( httpClientOptions ) );
//
//    }
//
//    @Test
//    public void getSimpleKBar() throws Exception{
//    }
//
//
//
//    @Test
//    public void getSimpleKBarEx() throws Exception{
//
//
//        provider.getSimpleKBarEx( "6008438", KLineType.DAY,100,
//                LocalDate.parse( "2017-07-14" ),
//                LocalDate.parse( "2017-07-17" ),null );
//        Thread.sleep( 10000000 );
//    }
//
//    @Test
//    public void getCurrentKbar() throws Exception{
//        provider.getCurrentKbar( "600848",res->{
//            System.out.println(res);
//        } );
//        Thread.sleep( 10000000 );
//
//    }
//
//}