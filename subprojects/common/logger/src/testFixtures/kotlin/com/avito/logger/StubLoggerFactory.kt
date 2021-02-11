package com.avito.logger

object StubLoggerFactory : LoggerFactory {

    override fun create(tag: String): Logger = StubLogger(tag)
}
