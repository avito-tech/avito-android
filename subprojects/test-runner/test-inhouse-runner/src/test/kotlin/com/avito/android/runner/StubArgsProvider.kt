package com.avito.android.runner

import com.avito.android.test.report.arguments.ArgsProvider
import java.io.Serializable

internal class StubArgsProvider : ArgsProvider {

    private val args = mutableMapOf<String, Any>()

    fun add(name: String, value: String) {
        args[name] = value
    }

    fun add(name: String, value: Int) {
        args[name] = value
    }

    override fun getOptionalArgument(name: String): String? {
        return args[name] as? String
    }

    override fun getMandatoryArgument(name: String): String {
        return args[name] as String
    }

    override fun <T : Serializable> getMandatorySerializableArgument(name: String): T {
        TODO("Not yet implemented")
    }

    override fun <T : Serializable> getOptionalSerializableArgument(name: String): T? {
        TODO("Not yet implemented")
    }
}
