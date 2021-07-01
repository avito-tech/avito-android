package com.avito.time

import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * Used in android runtime < 26, so no java.time API yet
 * consider using https://developer.android.com/studio/write/java8-support#library-desugaring
 */
public class DefaultTimeProvider : TimeProvider {

    override fun nowInMillis(): Long = System.currentTimeMillis()

    override fun nowInSeconds(): Long = TimeUnit.MILLISECONDS.toSeconds(nowInMillis())

    override fun now(): Date = toDate(nowInSeconds())

    override fun toDate(seconds: Long): Date {
        val millis = TimeUnit.SECONDS.toMillis(seconds)
        return Date(millis)
    }
}
