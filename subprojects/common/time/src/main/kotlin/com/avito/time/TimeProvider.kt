package com.avito.time

import androidx.annotation.RequiresApi
import java.io.Serializable
import java.time.Instant
import java.util.Date

public interface TimeProvider : Serializable {

    /**
     * Use only in Android because old Java; otherwise use "nowInstant()"
     */
    public fun nowInMillis(): Long

    @RequiresApi(26)
    public fun nowInstant(): Instant

    /**
     * Use only in Android because old Java; otherwise use "nowInstant()"
     */
    public fun nowInSeconds(): Long

    /**
     * Use only in Android because old Java; otherwise use "nowInstant()"
     */
    public fun now(): Date

    public fun toDate(seconds: Long): Date
}
