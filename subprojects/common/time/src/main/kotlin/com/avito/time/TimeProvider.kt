package com.avito.time

import java.io.Serializable
import java.util.Date

interface TimeProvider : Serializable {

    fun nowInMillis(): Long

    fun nowInSeconds(): Long

    fun now(): Date

    fun toDate(seconds: Long): Date
}
