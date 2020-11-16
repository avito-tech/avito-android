package com.avito.android.runner.annotation.resolver

import com.test.fixtures.TestAnnotation1
import com.test.fixtures.TestAnnotation2
import com.test.fixtures.TestAnnotation3
import com.test.fixtures.TestAnnotation4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import ru.avito.util.matcher.containsValueWithType

class AnnotationsTest {

    @Test
    fun `getAnnotationsSubset - only subset item returned`() {
        assertThat(
            Annotations.getAnnotationsSubset(
                ClassWithAnnotation::class.java,
                null,
                TestAnnotation1::class.java
            ),
            allOf(
                hasSize(1),
                containsValueWithType<TestAnnotation1>()
            )
        )
    }

    @Test
    fun `getAnnotationsSubset - only used subset items returned`() {
        assertThat(
            Annotations.getAnnotationsSubset(
                ClassWithAnnotation::class.java,
                ClassWithAnnotation::class.java.getMethod("method"),
                TestAnnotation1::class.java,
                TestAnnotation2::class.java,
                TestAnnotation3::class.java,
                TestAnnotation4::class.java
            ),
            allOf(
                hasSize(3),
                containsValueWithType<TestAnnotation1>(),
                containsValueWithType<TestAnnotation2>(),
                containsValueWithType<TestAnnotation3>()
            )
        )
    }

    @Test
    fun `getAnnotationsSubset - empty - for empty subset`() {
        assertThat(
            Annotations.getAnnotationsSubset(
                ClassWithAnnotation::class.java,
                ClassWithAnnotation::class.java.getMethod("method")
            ),
            Matchers.empty()
        )
    }

    @Test
    fun `getAnnotationsSubset - method annotation wins`() {
        val annotations = Annotations.getAnnotationsSubset(
            ClassWithAnnotation::class.java,
            ClassWithAnnotation::class.java.getMethod("method"),
            TestAnnotation3::class.java
        )
        assertThat(
            annotations,
            allOf(
                hasSize(1),
                containsValueWithType<TestAnnotation3>()
            )
        )
        assertThat(
            (annotations[0] as TestAnnotation3).value,
            Matchers.`is`("method")
        )
    }
}

@TestAnnotation1
@TestAnnotation2
@TestAnnotation3("class")
private class ClassWithAnnotation {

    @TestAnnotation3("method")
    fun method() {
    }
}
