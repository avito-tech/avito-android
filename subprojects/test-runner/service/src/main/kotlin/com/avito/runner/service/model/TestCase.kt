package com.avito.runner.service.model

import com.avito.report.model.DeviceName
import com.avito.report.model.TestName
import java.io.Serializable

data class TestCase(
    val name: TestName,
    val deviceName: DeviceName
) : Serializable {

    override fun toString(): String {
        return "$name.$deviceName"
    }

    companion object
}
