package com.avito.android.test.util

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class ReflectionTests {

    @Test
    fun `getFieldByReflection returns value of private field`() {
        class Holder {
            @Suppress("UnusedPrivateMember", "VarCouldBeVal")
            private var field: String = "Lorem Ipsum"
        }

        val holder = Holder()
        val value = holder.getFieldByReflection<String>("field")
        assertThat(value).isEqualTo("Lorem Ipsum")
    }
}
