package com.avito.android.test.report

import androidx.test.espresso.NoMatchingViewException
import com.avito.utils.ResourcesReader
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.reflect.Constructor

internal class ReportFriendlyFailureHandlerTest {

    @Test
    fun `failureHandler - removes view hierarchy`() {
        val exception = assertThrows<NoMatchingViewException> {
            ReportFriendlyFailureHandler().handle(
                error = createException<NoMatchingViewException>(
                    ResourcesReader.readText("view-hierarchy.txt")
                ),
                viewMatcher = null
            )
        }

        assertThat(exception.message).isEqualTo(
            "Не найдена view в иерархии: \"(is descendant of a: " +
                "with id: com.avito.android.stagingautotest:id/layout_delivery_buttons and with id: 2131363381)\""
        )
    }
}

private inline fun <reified T : Throwable> createException(message: String): T {
    val factoryClass = Class.forName("sun.reflect.ReflectionFactory")
    val factory = factoryClass.getMethod("getReflectionFactory").invoke(null)
    val constructor = factoryClass
        .getMethod("newConstructorForSerialization", Class::class.java, Constructor::class.java)
        .invoke(factory, T::class.java, RuntimeException::class.java.getDeclaredConstructor(String::class.java))
        as Constructor<*>
    return T::class.java.cast(constructor.newInstance(message))!!
}
