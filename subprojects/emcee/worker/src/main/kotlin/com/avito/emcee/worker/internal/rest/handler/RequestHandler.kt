package com.avito.emcee.worker.internal.rest.handler

import com.avito.emcee.worker.internal.rest.HttpMethod

internal open class RequestHandler(val method: HttpMethod, val path: String, val response: () -> String)
