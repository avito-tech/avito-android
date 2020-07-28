package com.avito.runner.logging

import com.avito.logger.Logger

class StdOutLogger : Logger {

    override fun debug(msg: String) {
        println(msg)
    }

    override fun info(msg: String) {
        println(msg)
    }

    override fun warn(msg: String, error: Throwable?) {
        println("WARN: $msg ${error?.message}")
    }

    override fun exception(msg: String, error: Throwable) {
        println("ERROR: $msg ${error?.message}")
    }

    override fun critical(msg: String, error: Throwable) {
        println("ERROR: $msg ${error?.message}")
    }
}
