package com.avito.android.test.report

import android.os.Bundle
import java.io.Serializable

interface ArgsProvider {

    /**
     * Blank strings also handled and will return null
     */
    fun getOptionalArgument(name: String): String?

    fun getMandatoryArgument(name: String): String

    fun getMandatorySerializableArgument(name: String): Serializable
}

class BundleArgsProvider(
    private val bundle: Bundle
) : ArgsProvider {

    override fun getMandatorySerializableArgument(name: String): Serializable {
        val result: Serializable? = bundle.getSerializable(name)

        if (result == null) {
            throw ReporterException("$name is a mandatory serializable argument")
        } else {
            return result
        }
    }

    override fun getOptionalArgument(name: String): String? =
        bundle.getString(name)?.let { if (it.isBlank()) null else it }

    override fun getMandatoryArgument(name: String): String {
        val result: String? = bundle.getString(name)
        if (result == null || result.isBlank()) {
            throw ReporterException(
                "$name is a mandatory argument; available keys=${bundle.keySet().joinToString(separator = ", ")}"
            )
        } else {
            return result
        }
    }
}
