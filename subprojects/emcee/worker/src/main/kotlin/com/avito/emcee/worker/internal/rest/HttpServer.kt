package com.avito.emcee.worker.internal.rest

import com.avito.emcee.worker.internal.rest.handler.RequestHandler
import io.ktor.serialization.gson.gson
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import java.util.logging.Logger

internal class HttpServer(
    private val handlers: List<RequestHandler<*>>,
    private val port: Int,
    private val gracePeriodMs: Long = 500,
    private val timeoutMs: Long = 500,
) {

    private var server: NettyApplicationEngine? = null

    private val logger = Logger.getLogger(HttpServer::class.simpleName)

    fun start() {
        logger.info("Starting REST server at $port port")
        server = embeddedServer(Netty, port = port) {
            install(ContentNegotiation) {
                gson() // TODO: use Moshi instead when it will be available
            }
            routing {
                handlers.forEach { handler: RequestHandler<*> ->
                    route(handler.path, handler.method) {
                        handle {
                            call.respond(handler.response())
                        }
                    }
                }
            }
        }
        requireNotNull(server).start(wait = false)
    }

    fun stop() {
        val server = requireNotNull(server) {
            "HttpServer is not started yet!"
        }
        logger.info("REST server has been stopped")
        server.stop(gracePeriodMs, timeoutMs)
    }
}
