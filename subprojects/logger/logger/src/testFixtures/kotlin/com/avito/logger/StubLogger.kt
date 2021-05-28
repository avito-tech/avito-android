package com.avito.logger

class StubLogger(private val tag: String) : Logger {

    private val isRunFromIde = true

    override fun debug(msg: String) {
        if (isRunFromIde) {
            println("[$tag] $msg")
        }
    }

    override fun info(msg: String) {
        if (isRunFromIde) {
            println("[$tag] $msg")
        }
    }

    override fun warn(msg: String, error: Throwable?) {
        if (isRunFromIde) {
            println("[$tag] $msg")
            error?.printStackTrace()
        }
    }

    override fun critical(msg: String, error: Throwable) {
        if (isRunFromIde) {
            println("[$tag] $msg")
            error.printStackTrace()
        }
    }
}
