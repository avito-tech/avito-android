package com.avito.android.instant_feedback

import com.avito.android.instant_feedback.internal.client.FeedbackClient
import com.avito.android.instant_feedback.internal.client.FeedbackClientImpl
import com.avito.android.instant_feedback.internal.config.ConfigFactory
import com.avito.android.instant_feedback.internal.config.EnvBasedConfigFactory
import com.avito.android.instant_feedback.internal.handler.HealthCheckHandler
import com.avito.android.instant_feedback.internal.handler.WebhookHandler
import com.avito.logger.LoggerFactory
import com.avito.logger.PrintlnLoggerFactory
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.time.Duration
import java.time.temporal.ChronoUnit

public fun main() {
    embeddedServer(Netty, port = 8080) {
        configureRouting(EnvBasedConfigFactory, PrintlnLoggerFactory)
    }.start(wait = true)
}

internal fun Application.configureRouting(
    configFactory: ConfigFactory,
    loggerFactory: LoggerFactory
) {

    val config = configFactory.create().getOrThrow()

    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(Duration.of(30, ChronoUnit.SECONDS))
        .readTimeout(Duration.of(30, ChronoUnit.SECONDS))
        .writeTimeout(Duration.of(30, ChronoUnit.SECONDS))
        .build()

    val feedbackClient: FeedbackClient = FeedbackClientImpl(
        url = config.serviceEndpoint,
        json = json,
        okHttpClient = okHttpClient,
        loggerFactory = loggerFactory,
    )

    install(ContentNegotiation) {
        json(json)
    }

    val webhookHandler = WebhookHandler(
        json = json,
        feedbackClient = feedbackClient,
        loggerFactory = loggerFactory,
    )

    routing {
        get("/health", HealthCheckHandler.action)
        post("/webhooks/repository", webhookHandler.action)
    }
}
