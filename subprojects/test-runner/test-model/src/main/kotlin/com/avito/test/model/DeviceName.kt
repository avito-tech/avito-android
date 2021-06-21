package com.avito.test.model

import java.io.Serializable

public data class DeviceName(val name: String) : Serializable {

    override fun toString(): String = name
}
