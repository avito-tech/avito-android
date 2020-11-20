package ru.avito.util

import junit.framework.AssertionFailedError

@Deprecated("Use assertThrows function with reified generic")
inline fun <T : Throwable> assertThrows(expectedType: Class<T>, executable: () -> Unit): T {
    try {
        executable()
    } catch (actualException: Throwable) {
        if (expectedType.isInstance(actualException)) {
            @Suppress("UNCHECKED_CAST")
            return actualException as T
        } else {
            throw AssertionFailedError(
                "Unexpected exception type thrown. " +
                        "Expected: $expectedType , but was ${actualException.javaClass}"
            )
        }
    }
    throw AssertionFailedError("Expected $expectedType to be thrown, but nothing was thrown.")
}

inline fun <reified T : Throwable> assertThrows(executable: () -> Unit): T {
    val expectedType = T::class.java
    @Suppress("DEPRECATION")
    return assertThrows(expectedType, executable)
}
