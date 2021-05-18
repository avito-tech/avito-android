package com.avito.android.test.report

import java.util.concurrent.TimeUnit

interface TimeSource {

    fun nowInSeconds(): Long

    class Impl : TimeSource {

        override fun nowInSeconds(): Long = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
    }
}
