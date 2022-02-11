package com.avito.android.test.report

import android.os.Bundle
import java.io.Serializable

// TODO: consider of using as a wrapper above "bundle" to protect from collisions, empty values, ...
class BundleArgsProvider(
    private val bundle: Bundle
) : ArgsProvider {

    override fun <T : Serializable> getSerializableArgumentOrThrow(name: String): T {
        val result: T? = getSerializableArgument(name)

        if (result == null) {
            throw IllegalStateException("$name is a mandatory serializable argument")
        } else {
            return result
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Serializable> getSerializableArgument(name: String): T? =
        bundle.getSerializable(name) as T?

    override fun getArgument(name: String): String? = bundle.getString(name)

    override fun getArgumentOrThrow(name: String): String {
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
