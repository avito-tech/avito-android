package com.avito.junit

import com.avito.test.model.TestName

public sealed interface Duration {
    public data class Executed(val value: Long) : Duration
    public data object Unknown : Duration
}

public sealed interface JunitReportTestCase {
    public val name: TestName
    public val caseId: Int?
    public val duration: Duration

    public data class Skipped(
        override val name: TestName,
        override val caseId: Int?,
        override val duration: Duration,
        public val skipReason: String,
    ) : JunitReportTestCase

    public data class Success(
        override val name: TestName,
        override val caseId: Int?,
        override val duration: Duration
    ) : JunitReportTestCase

    /**
     *
     */
    public data class Failed(
        override val name: TestName,
        override val caseId: Int?,
        override val duration: Duration,
        public val error: String,
    ) : JunitReportTestCase

    /**
     *
     */
    public data class Error(
        override val name: TestName,
        override val caseId: Int?,
        override val duration: Duration,
        public val error: String,
    ) : JunitReportTestCase
}
