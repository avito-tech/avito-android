package com.avito.android.runner.args

import com.avito.android.test.report.ArgsProvider

public fun interface ArgProvider<out T> {
    public fun parse(args: ArgsProvider): T
}
