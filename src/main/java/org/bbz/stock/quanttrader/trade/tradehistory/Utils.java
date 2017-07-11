package org.bbz.stock.quanttrader.trade.tradehistory;

import io.vertx.core.http.HttpClient;

/**
 * Created by liukun on 2017/6/20.
 * 通过sinajs获取数据
 */
public class Utils{
//    INSTANCE;

    private HttpClient httpClient;

    Utils( HttpClient client ){
        this.httpClient = client;

    }

    /**
     * http://finance.sina.com.cn/realstock/company/sh600000/hisdata/klc_kl.js
     */
    void getXX(){
        httpClient.getNow( "finance.sina.com.cn", "/realstock/company/sh600000/hisdata/klc_kl.js",res->{
            res.bodyHandler( body->{
                parseBase64(body.toString());
            } );
        });
    }

    private void parseBase64( String body ){
//        new BASE64Decoder().decodeBuffer( body )
    }


}
