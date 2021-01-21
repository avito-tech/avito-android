package com.avito.time

import java.util.Date

interface TimeProvider {

    fun nowInMillis(): Long

    fun nowInSeconds(): Long

    fun now(): Date

    fun toDate(seconds: Long): Date
}
