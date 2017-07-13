package org.bbz.stock.quanttrader.trade.stockdata;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * Created by liulaoye on 17-6-29.
 * test
 */

//@RunWith(VertxUnitRunner.class)

public class RedisDataProviderTest{

    private RedisClient redis;

    @Before
    public void init(){
        final Vertx vertx = Vertx.vertx();

        RedisOptions config = new RedisOptions().setHost( "127.0.0.1" );
        redis = RedisClient.create( vertx, config );
    }

    @Test
    public void getRedisServerInfo() throws InterruptedException{
        redis.info( res -> {
            if( res.succeeded() ) {
                System.out.println( res.result() );
            } else {
                res.cause().printStackTrace();
            }
        } );
        final JsonObject put = new JsonObject().put( "6000109", "23.45" );
        System.out.println( put.toString() );
        redis.set( "stock", put.toString(), res -> {
            if( res.succeeded() ) {
                redis.get( "stock", res1 -> {
                    if( res1.succeeded() ) {
                        System.out.println( res1.result() );
                        System.out.println( res1.result().getClass() );
                    }
                } );
            } else {
                res.cause().printStackTrace();
            }
        } );
        Thread.sleep( 10000 );

    }

    /**
     * 测试读写二进制文件
     */
    @Test
    public void rwBin() throws InterruptedException{
        int count = 1000000;

        CountDownLatch latch = new CountDownLatch( count );
        long begin = System.nanoTime();
        final Buffer buffer = Buffer.buffer();
        for( int i = 0; i < count; i++ ) {
            redis.setBinary( "name", buffer.setInt(0, 10000000 ), res -> {

                if(res.succeeded()){
                    latch.countDown();
                }
            } );
        }
        latch.await();
        System.out.println( "操作耗时：" + (System.nanoTime() - begin) / 1000000000f + "秒" );

    }

    @Test
    public void x(){
        JsonObject jsonObject = new JsonObject();
        JsonObject attachement = new JsonObject().put( "s1","sv1" );
        jsonObject.put( "k1","v1" ).put(  "k2","v2").put( "k1","v3" );
//        jsonObject.put( attachement )
        System.out.println(jsonObject);
    }
    @Test
    public void benchmark() throws InterruptedException{


        int count = 1000000;
        CountDownLatch latch = new CountDownLatch( count );
        long begin = System.nanoTime();
        for( int i = 0; i < count; i++ ) {
            redis.set( "name", "10000000", res -> {
                if( res.succeeded() ) {
                    latch.countDown();
                }
            } );
//            redis.set( "name", "liulaoye" + i, null );

        }
        latch.await();
        System.out.println( "操作耗时：" + (System.nanoTime() - begin) / 1000000000f + "秒" );
        CountDownLatch latch1 = new CountDownLatch( count );
        for( int i = 0; i < count; i++ ) {
            redis.get( "name", res -> {
                if( res.succeeded() ) {
                    latch1.countDown();
                }
            } );
        }
        latch1.await();
        System.out.println( "操作耗时：" + (System.nanoTime() - begin) / 1000000000f + "秒" );

        redis.get( "name", res ->

        {
            if( res.succeeded() ) {
                System.out.println( res.result() );
            } else {
                res.cause().printStackTrace();
            }
        } );


    }
}