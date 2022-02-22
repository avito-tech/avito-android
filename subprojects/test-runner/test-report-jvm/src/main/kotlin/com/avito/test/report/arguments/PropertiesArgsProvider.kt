package com.avito.test.report.arguments

import com.avito.android.test.report.arguments.ArgsProvider
import java.io.Serializable
import java.util.Properties

public class PropertiesArgsProvider(private val properties: Properties) : ArgsProvider {

    override fun getOptionalArgument(name: String): String? {
        return properties.getProperty(name, "").let { it.ifBlank { null } }
    }

    override fun getMandatoryArgument(name: String): String {
        return properties.getProperty(name, "").let { it.ifBlank { error("$name is a mandatory argument") } }
    }

    override fun <T : Serializable> getMandatorySerializableArgument(name: String): T {
        throw UnsupportedOperationException("PropertiesArgsProvider does not support Serializable yet")
    }

    override fun <T : Serializable> getOptionalSerializableArgument(name: String): T? {
        throw UnsupportedOperationException("PropertiesArgsProvider does not support Serializable yet")
    }
}
