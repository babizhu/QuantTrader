package org.bbz.stock.quanttrader.vertx;//package org.bbz.stock.quanttrader.trade.stockdata;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.string.StringDecoder;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.impl.VertxImpl;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.impl.ChannelProvider;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

/**
 * 测试ChannelProvider类的使用
 */
public class ChannelProviderTest {

  @Test
  public void connect() throws InterruptedException {

    VertxImpl vertx = (VertxImpl) Vertx.vertx();
    Bootstrap bootstrap = new Bootstrap();
    bootstrap.group(new NioEventLoopGroup(1));
    bootstrap.channel(NioSocketChannel.class);
    HttpClientOptions options = new HttpClientOptions();
    options.setReusePort(true);
    vertx.transport().configure(options, bootstrap);

    SocketAddress remoteAddress = SocketAddress.inetSocketAddress(80, "www.sina.com.cn");
    ChannelProvider.INSTANCE.connect(vertx, bootstrap, null, remoteAddress,
        ch -> {
          ChannelPipeline pipeline = ch.pipeline();
          pipeline.addLast(new StringDecoder());
//          pipeline.addLast(new HttpClientCodec());
//          pipeline.addLast(new StringEncoder());
          pipeline.addLast(new HttpRequestEncoder());
          pipeline.addLast(new SimpleChannelInboundHandler<String>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, String msg) {
              System.out.println(msg);
//              ctx.writeAndFlush(msg);
            }

            @Override
            public void handlerAdded(ChannelHandlerContext ctx) {
              System.out.println("ChannelProviderTest.handlerAdded");
            }
          });
        },
        res -> {
          if (res.succeeded()) {
            Channel channel = res.result();
            System.out.println("连接成功,开始发送数据");
//            channel.writeAndFlush("abcd");
            FullHttpRequest req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
            req.headers().set("Host", "www.sina.com.cn");
            channel.writeAndFlush(req);
          }
        }
    );
//    Thread.sleep(1000000);
    vertx.nettyEventLoopGroup().awaitTermination(100000, TimeUnit.DAYS);
  }


}