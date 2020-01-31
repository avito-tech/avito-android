package com.avito.runner.logging

class StdOutLogger : Logger {

    override fun notify(message: String, error: Throwable?) {
        println("[error] $message ${error?.message}")
    }

    override fun log(message: String) {
        println(message)
    }
}
