package com.avito.logger

public class PrintlnLogger(private val tag: String) : Logger {

    override fun debug(msg: String) {
        println("DEBUG:[$tag] $msg")
    }

    override fun info(msg: String) {
        println("INFO:[$tag] $msg")
    }

    override fun warn(msg: String, error: Throwable?) {
        println("WARN:[$tag] $msg")
        error?.printStackTrace()
    }

    override fun critical(msg: String, error: Throwable) {
        println("CRITICAL:[$tag] $msg")
        error.printStackTrace()
    }
}
