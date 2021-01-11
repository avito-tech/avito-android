package com.avito.logger

object StubLogger : Logger {

    private val isRunFromIde = System.getProperty("isInvokedFromIde") == "true"

    override fun debug(msg: String) {
        if (isRunFromIde) {
            println(msg)
        }
    }

    override fun info(msg: String) {
        if (isRunFromIde) {
            println(msg)
        }
    }

    override fun warn(msg: String, error: Throwable?) {
        if (isRunFromIde) {
            println(msg)
            error?.printStackTrace()
        }
    }

    override fun critical(msg: String, error: Throwable) {
        if (isRunFromIde) {
            println(msg)
            error.printStackTrace()
        }
    }
}
