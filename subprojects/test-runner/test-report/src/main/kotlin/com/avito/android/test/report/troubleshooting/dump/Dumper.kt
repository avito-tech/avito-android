package com.avito.android.test.report.troubleshooting.dump

public interface Dumper {
    public val label: String
    public fun dump(): String
}
