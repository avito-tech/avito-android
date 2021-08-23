package com.avito.time

import androidx.annotation.RequiresApi
import java.io.Serializable
import java.time.Instant
import java.util.Date

public interface TimeProvider : Serializable {

    @Deprecated("Use only in Android because old Java", replaceWith = ReplaceWith("nowInstant()"))
    public fun nowInMillis(): Long

    @RequiresApi(26)
    public fun nowInstant(): Instant

    @Deprecated("Use only in Android because old Java", replaceWith = ReplaceWith("nowInstant()"))
    public fun nowInSeconds(): Long

    @Deprecated("Use only in Android because old Java", replaceWith = ReplaceWith("nowInstant()"))
    public fun now(): Date

    public fun toDate(seconds: Long): Date
}
