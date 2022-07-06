package com.avito.android.diff.report

import com.avito.slack.model.SlackChannel

public sealed class OwnersDiffReportDestination {

    public class File(public val parentDir: java.io.File) : OwnersDiffReportDestination()

    public class Slack(
        public val token: String,
        public val workspace: String,
        public val channel: SlackChannel,
        public val userName: String,
    ) : OwnersDiffReportDestination()

    public class Custom(public val reporter: OwnersDiffReporter) : OwnersDiffReportDestination()
}
