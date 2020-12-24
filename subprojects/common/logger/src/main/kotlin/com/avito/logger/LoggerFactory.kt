package com.avito.logger

interface LoggerFactory {

    fun create(tag: String): Logger
}

inline fun <reified T> LoggerFactory.create(): Logger = create(tag = T::class.java.simpleName)
