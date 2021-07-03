package com.avito.retrace

public object Stub : ProguardRetracer {

    override fun retrace(content: String): String = content
}
