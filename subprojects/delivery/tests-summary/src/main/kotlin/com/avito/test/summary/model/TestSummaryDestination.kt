package com.avito.test.summary.model

import com.avito.alertino.model.AlertinoRecipient
import com.avito.report.model.Team

internal data class TestSummaryDestination(
    val teamName: Team,
    val channel: AlertinoRecipient,
)
