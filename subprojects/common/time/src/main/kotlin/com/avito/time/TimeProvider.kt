package com.avito.time

import java.io.Serializable
import java.util.Date

public interface TimeProvider : Serializable {

    public fun nowInMillis(): Long

    public fun nowInSeconds(): Long

    public fun now(): Date

    public fun toDate(seconds: Long): Date
}
