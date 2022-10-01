package com.avito.emcee.worker.internal.rest.handler

import io.ktor.http.HttpMethod

// A simple way to check if worker is alive using HTTP request
internal object HealthCheckRequestHandler : RequestHandler<String>(
    method = HttpMethod.Get,
    path = "/healthCheck",
    response = { "I'm alive" }
)
