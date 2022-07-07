package com.avito.emcee.worker.internal.rest

internal fun String.wrapToSuccessfulResponse(): String =
    """
       |HTTP/1.0 200 OK
       |Content-Type: application/json
       |Content-Length: ${this.length}
       |
       |$this
    """.trimMargin()

internal fun badRequest(): String =
    """
        |HTTP/1.0 400 Bad Request
        |
        |
    """.trimMargin()
