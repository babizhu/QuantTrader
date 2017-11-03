package org.bbz.stock.quanttrader.kotlin

import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.http.HttpClientResponse
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.awaitEvent

//https://github.com/vert-x3/vertx-examples/blob/master/kotlin-examples/coroutines/src/main/kotlin/movierating/App.kt
class KotlinApp : CoroutineVerticle() {

    private lateinit var client: HttpClient
    private lateinit var dbClient: JDBCClient

    override suspend
    fun start() {
        dbClient = JDBCClient.createShared(vertx, json {
            obj(
                    "url" to "jdbc:hsqldb:mem:test?shutdown=true",
                    "driver_class" to "org.hsqldb.jdbcDriver",
                    "max_pool_size-loop" to 30
            )
        })
        val httpClientOptions = HttpClientOptions()

        httpClientOptions.setDefaultPort(80).setDefaultHost("www.sina.com.cn").setConnectTimeout(4000).isKeepAlive = true
        client = vertx.createHttpClient(httpClientOptions)
//        client["/", { resp ->
//            resp.exceptionHandler({ exception -> print(exception) })
//            resp.bodyHandler({ msg ->
//                print(msg)
//
//            })
//        }].end()
        t1()



    }

    private suspend fun t1() {
        val result = awaitEvent<HttpClientResponse> { client["/", it].end() }
        println(2222222222222)
        result.bodyHandler({msg-> print(msg)})
    }
}