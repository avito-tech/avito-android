package com.avito.runner.logging

interface Logger {
    fun log(message: String)
    fun notify(message: String, error: Throwable? = null)
}
