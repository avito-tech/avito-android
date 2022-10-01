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

internal class HttpServer(
    private val handlers: List<RequestHandler<*>>,
    private val debug: Boolean
) {

    private var server: NettyApplicationEngine? = null

    fun start(port: Int) {
        if (debug) println("Starting REST server on $port port/")
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
        requireNotNull(server) {
            "HttpServer is not started yet!"
        }.stop()
    }

    class Builder {

        private val handlers: MutableList<RequestHandler<*>> = mutableListOf()
        private var debug = false

        fun addHandler(handler: RequestHandler<*>) = apply {
            handlers.add(handler)
        }

        fun debug(value: Boolean): Builder = apply {
            debug = value
        }

        fun build(): HttpServer = HttpServer(handlers, debug)
    }
}
