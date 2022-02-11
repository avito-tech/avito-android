package com.avito.android.runner

import com.avito.android.test.report.ArgsProvider
import java.io.Serializable

internal class StubArgsProvider : ArgsProvider {

    private val args = mutableMapOf<String, Any>()

    fun add(name: String, value: String) {
        args[name] = value
    }

    fun add(name: String, value: Int) {
        args[name] = value
    }

    override fun getArgument(name: String): String? {
        return args[name] as? String
    }

    override fun getArgumentOrThrow(name: String): String {
        return args[name] as String
    }

    override fun <T : Serializable> getSerializableArgumentOrThrow(name: String): T {
        TODO("Not yet implemented")
    }

    override fun <T : Serializable> getSerializableArgument(name: String): T? {
        TODO("Not yet implemented")
    }
}
