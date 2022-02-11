package com.avito.android.test.report.arguments

import java.io.Serializable

public interface ArgsProvider {

    /**
     * Blank strings also handled and will return null
     */
    public fun getOptionalArgument(name: String): String?

    public fun getMandatoryArgument(name: String): String

    public fun <T : Serializable> getMandatorySerializableArgument(name: String): T

    public fun <T : Serializable> getOptionalSerializableArgument(name: String): T?
}
