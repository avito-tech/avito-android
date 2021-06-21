package com.avito.report.model

import java.io.Serializable

public data class DeviceName(val name: String) : Serializable {

    override fun toString(): String = name
}
