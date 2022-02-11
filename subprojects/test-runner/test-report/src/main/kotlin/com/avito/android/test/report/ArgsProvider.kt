package com.avito.android.test.report

import java.io.Serializable

public interface ArgsProvider {

    /**
     * Blank strings also handled and will return null
     */
    public fun getArgument(name: String): String?

    public fun getArgumentOrThrow(name: String): String

    public fun <T : Serializable> getSerializableArgumentOrThrow(name: String): T

    public fun <T : Serializable> getSerializableArgument(name: String): T?

    @Deprecated(
        message = "getOptionalArgument is deprecated, use getArgument",
        replaceWith = ReplaceWith("getArgument(name)")
    )
    public fun getOptionalArgument(name: String): String? = getArgument(name)

    @Deprecated(
        message = "getMandatoryArgument is deprecated, use getArgumentOrThrow",
        replaceWith = ReplaceWith("getArgumentOrThrow(name)")
    )
    public fun getMandatoryArgument(name: String): String? = getArgumentOrThrow(name)

    @Deprecated(
        message = "getMandatorySerializableArgument is deprecated, use getSerializableArgumentOrThrow",
        replaceWith = ReplaceWith("getSerializableArgumentOrThrow(name)")
    )
    public fun <T : Serializable> getMandatorySerializableArgument(name: String): T =
        getSerializableArgumentOrThrow(name)

    @Deprecated(
        message = "getMandatorySerializableArgument is deprecated, use getSerializableArgument",
        replaceWith = ReplaceWith("getSerializableArgument(name)")
    )
    public fun <T : Serializable> getOptionalSerializableArgument(name: String): T? = getSerializableArgument(name)
}
