package com.avito.android.test.report.arguments

import android.os.Bundle
import java.io.Serializable

// TODO: consider of using as a wrapper above "bundle" to protect from collisions, empty values, ...
class BundleArgsProvider(
    private val bundle: Bundle
) : ArgsProvider {

    override fun <T : Serializable> getMandatorySerializableArgument(name: String): T {
        val result: T? = getOptionalSerializableArgument(name)

        if (result == null) {
            throw IllegalStateException("$name is a mandatory serializable argument")
        } else {
            return result
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Serializable> getOptionalSerializableArgument(name: String): T? =
        bundle.getSerializable(name) as T?

    override fun getOptionalArgument(name: String): String? =
        bundle.getString(name)?.let { if (it.isBlank()) null else it }

    override fun getMandatoryArgument(name: String): String {
        val result: String? = bundle.getString(name)
        if (result == null || result.isBlank()) {
            throw IllegalStateException(
                "$name is a mandatory argument; all values=$bundle"
            )
        } else {
            return result
        }
    }
}
