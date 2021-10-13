package com.avito.logger

import java.io.Serializable

public object PrintlnLoggerFactory : LoggerFactory, Serializable {

    private val isRunFromIde = System.getProperty("isInvokedFromIde") == "true"

    override fun create(tag: String): Logger = if (isRunFromIde) {
        PrintlnLogger(tag)
    } else {
        NoOpLogger
    }
}
