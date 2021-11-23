package com.avito.android.info

public open class BuildPropertiesExtension {

    internal val properties = mutableMapOf<String, String>()

    public fun buildProperty(name: String, value: String) {
        properties[name] = value
    }
}
