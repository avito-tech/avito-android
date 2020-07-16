package com.avito.logger

object FakeLogger: Logger {

    override fun debug(msg: String) {}

    override fun warn(msg: String) {}

    override fun exception(msg: String, error: Throwable) {}

    override fun critical(msg: String, error: Throwable) {}
}
