package com.orange.camara.fga.reverseproxy

import io.vertx.core.Future
import io.vertx.core.buffer.Buffer
import io.vertx.httpproxy.Body
import io.vertx.httpproxy.ProxyContext
import io.vertx.httpproxy.ProxyInterceptor
import io.vertx.httpproxy.ProxyResponse
import jakarta.enterprise.context.ApplicationScoped
import java.util.logging.Logger


@ApplicationScoped
class AuthorizationInterceptor: ProxyInterceptor {

    private val logger: Logger = Logger.getLogger(this.javaClass.simpleName)

    private var toggle = false

    override fun handleProxyRequest(context: ProxyContext): Future<ProxyResponse> {
        return context.request()
            .proxiedRequest()
            .resume()
            .body()
            .compose { buffer ->
                // Reset body to be able to re-use it in proxy
                context.request().body = Body.body(buffer)
                if (isValid(buffer)) {
                    logger.info("Valid body")
                    context.sendRequest()
                } else {
                    logger.severe("Invalid body")
                    val proxyResponse: ProxyResponse = context.request().response().setStatusCode(403)
                    Future.succeededFuture(proxyResponse)
                }
            }
    }

    override fun handleProxyResponse(context: ProxyContext): Future<Void> {
        logger.info("response code: ${context.response().statusCode}")
        return super.handleProxyResponse(context)
    }

    fun isValid(buffer: Buffer): Boolean {
        toggle = !toggle
        return toggle
    }
}
