package com.avito.android.plugin.build_metrics.internal.runtime

internal fun Long.bytesToKiB(): Int = (this / 1024).toInt()
