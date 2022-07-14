package com.avito.android.plugin.build_metrics.internal.jvm.command

import java.io.File

internal fun javaHome(): File =
    File(System.getProperty("java.home"))
