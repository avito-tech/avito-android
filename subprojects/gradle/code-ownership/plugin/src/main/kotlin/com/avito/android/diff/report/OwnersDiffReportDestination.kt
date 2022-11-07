package com.avito.android.diff.report

public sealed class OwnersDiffReportDestination {

    public class File(public val parentDir: java.io.File) : OwnersDiffReportDestination()

    public class Custom(public val reporter: OwnersDiffReporter) : OwnersDiffReportDestination()
}
