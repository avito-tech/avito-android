package com.avito.runner.service.model

import com.avito.test.model.DeviceName
import com.avito.test.model.TestName
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
