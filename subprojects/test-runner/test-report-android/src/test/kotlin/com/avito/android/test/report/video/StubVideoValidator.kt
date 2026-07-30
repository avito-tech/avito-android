package com.avito.android.test.report.video

import java.io.File

internal class StubVideoValidator(
    private val readableAtAttempt: Int = 1
) : VideoValidator {

    var attempts: Int = 0
        private set

    override fun isReadable(video: File): Boolean {
        attempts++
        return attempts >= readableAtAttempt
    }

    companion object {

        fun neverReadable(): StubVideoValidator = StubVideoValidator(readableAtAttempt = Int.MAX_VALUE)
    }
}
