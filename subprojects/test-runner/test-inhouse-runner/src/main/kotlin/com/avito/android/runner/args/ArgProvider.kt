package com.avito.android.runner.args

import com.avito.android.test.report.ArgsProvider

fun interface ArgProvider<out T> {
    fun parse(args: ArgsProvider): T
}
