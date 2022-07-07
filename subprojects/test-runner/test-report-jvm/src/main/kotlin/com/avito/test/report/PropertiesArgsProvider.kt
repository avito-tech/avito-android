package com.avito.test.report

import com.avito.android.test.report.ArgsProvider
import java.io.Serializable
import java.util.Properties

public class PropertiesArgsProvider(private val properties: Properties) : ArgsProvider {

    override fun getArgument(name: String): String? {
        return properties.getProperty(name)
    }

    override fun getArgumentOrThrow(name: String): String {
        return properties.getProperty(name) ?: error("$name is a mandatory argument")
    }

    override fun <T : Serializable> getSerializableArgumentOrThrow(name: String): T {
        throw UnsupportedOperationException("PropertiesArgsProvider does not support Serializable yet")
    }

    override fun <T : Serializable> getSerializableArgument(name: String): T? {
        return null
    }
}
