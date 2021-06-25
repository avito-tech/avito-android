package com.avito.test.model

import java.io.Serializable

public data class TestCase(
  val name: TestName,
  val deviceName: DeviceName
) : Serializable {

    override fun toString(): String {
        return "$name.$deviceName"
    }

    public companion object
}
