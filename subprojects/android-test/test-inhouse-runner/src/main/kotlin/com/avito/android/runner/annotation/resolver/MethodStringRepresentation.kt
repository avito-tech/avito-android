package com.avito.android.runner.annotation.resolver

import com.avito.android.runner.annotation.resolver.MethodStringRepresentation.Resolution
import java.lang.reflect.Method

/**
 * Parser for string representation of methods of class: class#method
 */
internal object MethodStringRepresentation {

    sealed class Resolution {
        data class ClassOnly(val aClass: Class<*>) : Resolution()
        data class Method(val aClass: Class<*>, val method: java.lang.reflect.Method) : Resolution()
        data class ParseError(val message: String) : Resolution()
    }

    fun parseString(stringRepresentation: String): Resolution {
        if (stringRepresentation.trim().isEmpty()) {
            return Resolution.ParseError("Method string representation is empty string")
        }

        val testTargetString = stringRepresentation.split("#")

        return when (testTargetString.size) {
            1, 2 -> safeClassForName(
                testTargetString[0]
            ) { aClass ->
                if (testTargetString.size == 1) {
                    Resolution.ClassOnly(
                        aClass
                    )
                } else {
                    aClass.safeMethodForName(testTargetString[1])
                }
            }
            else -> Resolution.ParseError(
                "Invalid method string representation: $stringRepresentation"
            )
        }
    }

    private fun safeClassForName(
        className: String,
        onError: (Throwable) -> Resolution = {
            Resolution.ParseError(
                "Can't find class $className"
            )
        },
        onSuccess: (Class<*>) -> Resolution
    ): Resolution {
        return try {
            onSuccess(Class.forName(className))
        } catch (e: ClassNotFoundException) {
            onError(e)
        }
    }

    private fun Class<*>.safeMethodForName(
        methodName: String,
        onError: (Throwable) -> Resolution = {
            Resolution.ParseError(
                "Can't find method $methodName in class $name"
            )
        },
        onSuccess: (Method) -> Resolution = {
            Resolution.Method(
                this,
                it
            )
        }
    ): Resolution {
        return try {
            onSuccess(getMethod(methodName))
        } catch (e: NoSuchMethodException) {
            onError(e)
        }
    }
}

internal fun Resolution.getClassOrThrow(): Class<*> {
    return when (this) {
        is Resolution.ClassOnly -> aClass
        is Resolution.Method -> aClass
        is Resolution.ParseError -> throw IllegalArgumentException(message)
    }
}
