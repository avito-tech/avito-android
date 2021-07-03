package com.avito.logger

public interface LoggerFactory {

    public fun create(tag: String): Logger
}

public inline fun <reified T> LoggerFactory.create(): Logger = create(tag = T::class.java.simpleName)
