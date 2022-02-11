package com.avito.android.test.report

import java.util.concurrent.TimeUnit

public interface TimeSource {

    public fun nowInSeconds(): Long

    public class Impl : TimeSource {

        override fun nowInSeconds(): Long = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
    }
}
