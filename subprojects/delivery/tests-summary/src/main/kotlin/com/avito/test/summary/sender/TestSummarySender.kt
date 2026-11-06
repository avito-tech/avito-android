package com.avito.test.summary.sender

import com.avito.test.summary.model.CrossDeviceSuite

internal interface TestSummarySender {
    fun send(suite: CrossDeviceSuite, reportId: String)
}
