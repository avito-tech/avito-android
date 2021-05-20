package com.avito.logger

object NoOpLogger : Logger {

    override fun debug(msg: String) {
    }

    override fun info(msg: String) {
    }

    override fun warn(msg: String, error: Throwable?) {
    }

    override fun critical(msg: String, error: Throwable) {
    }
}
