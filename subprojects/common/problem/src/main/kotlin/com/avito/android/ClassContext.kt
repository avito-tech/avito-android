package com.avito.android

data class ClassContext(val className: String, val whatsGoingOn: String = "") {

    override fun toString(): String = "$className $whatsGoingOn"
}
