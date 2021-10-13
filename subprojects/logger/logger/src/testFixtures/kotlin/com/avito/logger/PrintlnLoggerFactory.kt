package com.avito.logger

import java.io.Serializable

public object PrintlnLoggerFactory : LoggerFactory, Serializable {

    override fun create(tag: String): Logger = PrintlnLogger(tag)
}
