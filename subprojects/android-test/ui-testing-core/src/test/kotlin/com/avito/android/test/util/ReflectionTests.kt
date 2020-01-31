package com.avito.android.test.util

import org.hamcrest.Matchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test

class ReflectionTests {

    @Test
    fun `getFieldByReflection returns value of private field`() {
        class Holder {
            @Suppress("UnusedPrivateMember", "VarCouldBeVal")
            private var field: String = "Lorem Ipsum"
        }

        val holder = Holder()
        val value = holder.getFieldByReflection<String>("field")
        assertThat(value, equalTo("Lorem Ipsum"))
    }
}
