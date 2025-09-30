package com.avito.android.instant_feedback.internal.handler

import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.util.pipeline.PipelineInterceptor

internal object HealthCheckHandler {
    val action: PipelineInterceptor<Unit, ApplicationCall> = {
        call.respond(mapOf("status" to "ok"))
    }
}
