package com.avito.emcee.worker.internal.rest.handler

import io.ktor.http.HttpMethod

internal open class RequestHandler<T : Any>(val method: HttpMethod, val path: String, val response: () -> T)
