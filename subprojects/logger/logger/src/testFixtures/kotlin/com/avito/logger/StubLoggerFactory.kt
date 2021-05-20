package com.avito.logger

import java.io.Serializable

object StubLoggerFactory : LoggerFactory, Serializable {

    override fun create(tag: String): Logger = StubLogger(tag)
}
