package com.avito.android.build_verdict.internal.writer

import com.avito.android.build_verdict.internal.BuildVerdict

internal interface BuildVerdictWriter {
    fun write(buildVerdict: BuildVerdict)
}
