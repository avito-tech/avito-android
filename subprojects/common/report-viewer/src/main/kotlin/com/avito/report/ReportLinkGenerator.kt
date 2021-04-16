package com.avito.report

import com.avito.report.model.Team
import com.avito.report.model.TestName

interface ReportLinkGenerator {

    fun generateReportLink(filterOnlyFailtures: Boolean = false, team: Team = Team.UNDEFINED): String

    fun generateTestLink(testName: TestName): String

    object Stub : ReportLinkGenerator {

        override fun generateReportLink(filterOnlyFailtures: Boolean, team: Team): String = ""

        override fun generateTestLink(testName: TestName): String = ""
    }
}
