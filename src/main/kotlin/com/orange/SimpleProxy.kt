package com.orange.camara.fga.reverseproxy

import io.vertx.core.Vertx
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpClientOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.proxy.handler.ProxyHandler
import io.vertx.httpproxy.HttpProxy
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes

@ApplicationScoped
class SimpleProxy(private val authorizationInterceptor: AuthorizationInterceptor) {

    @SuppressWarnings
    fun onStart(@Observes router: Router, vertx: Vertx) {
        val proxyClient: HttpClient = vertx.createHttpClient(HttpClientOptions().setTrustAll(false))

        val proxy: HttpProxy = HttpProxy.reverseProxy(proxyClient)
        proxy.origin(ORIGIN_PORT, ORIGIN_HOST)
            .addInterceptor(authorizationInterceptor)

        router
            .route()
            .path("/authorize/*")
            .handler(ProxyHandler.create(proxy))
    }

    companion object {
        const val ORIGIN_PORT = 8888
        const val ORIGIN_HOST = "localhost"
    }
}
