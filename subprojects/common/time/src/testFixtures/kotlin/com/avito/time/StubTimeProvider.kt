package com.avito.time

import java.time.Duration
import java.util.Date

public class StubTimeProvider : TimeProvider {

    private val timeProvider = DefaultTimeProvider()

    public lateinit var now: Date

    override fun nowInMillis(): Long = timeProvider.nowInMillis()

    override fun nowInDuration(): Duration = timeProvider.nowInDuration()

    override fun nowInSeconds(): Long = timeProvider.nowInSeconds()

    override fun now(): Date = now

    override fun toDate(seconds: Long): Date = timeProvider.toDate(seconds)
}
