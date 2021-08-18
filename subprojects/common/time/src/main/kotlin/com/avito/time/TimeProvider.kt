package com.avito.time

import androidx.annotation.RequiresApi
import java.io.Serializable
import java.time.Duration
import java.util.Date

public interface TimeProvider : Serializable {

    public fun nowInMillis(): Long

    @RequiresApi(26)
    public fun nowInDuration(): Duration

    public fun nowInSeconds(): Long

    public fun now(): Date

    public fun toDate(seconds: Long): Date
}
