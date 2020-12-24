package com.avito.http

import com.avito.logger.Logger
import okhttp3.logging.HttpLoggingInterceptor

class HttpLogger(private val logger: Logger) : HttpLoggingInterceptor.Logger {

    override fun log(message: String) {
        logger.debug(message)
    }
}
