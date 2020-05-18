package com.avito.android.info

open class BuildPropertiesExtension {

    internal val properties = mutableMapOf<String, String>()

    fun buildProperty(name: String, value: String) {
        properties[name] = value
    }

}
