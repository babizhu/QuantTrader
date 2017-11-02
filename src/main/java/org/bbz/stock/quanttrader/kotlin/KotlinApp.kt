package org.bbz.stock.quanttrader.kotlin

import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpClientOptions
import io.vertx.kotlin.coroutines.CoroutineVerticle


class KotlinApp : CoroutineVerticle() {

    private lateinit var client: HttpClient

    suspend override fun start() {
        println("start")
        val httpClientOptions = HttpClientOptions()

        httpClientOptions.setDefaultPort(80).setDefaultHost("www.sina.com.cn.a").setConnectTimeout(4000).isKeepAlive = true
        client = vertx.createHttpClient(httpClientOptions)
        client["/", { resp ->
            resp.exceptionHandler({ exception -> print(exception) })
            resp.bodyHandler({ msg ->
                print(msg)

            })
        }].end()


    }
}