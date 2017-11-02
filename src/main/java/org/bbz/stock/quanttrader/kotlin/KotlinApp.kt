package org.bbz.stock.quanttrader.kotlin

import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpClientOptions
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.awaitResult

//https://github.com/vert-x3/vertx-examples/blob/master/kotlin-examples/coroutines/src/main/kotlin/movierating/App.kt
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

        client["/", json {  }]

    }

    suspend fun t1() {
        val result = awaitResult<String> { client.queryWithParams("SELECT TITLE FROM MOVIE WHERE ID=?", json { array(id) }, it) }
        if (result.rows.size == 1) {
            ctx.response().end(json {
                obj("id" to id, "title" to result.rows[0]["TITLE"]).encode()
            })
        } else {
            ctx.response().setStatusCode(404).end()
        }
    }
}