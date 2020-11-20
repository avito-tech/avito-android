package com.avito.instrumentation.suite.dex

import com.google.common.collect.ImmutableList
import com.google.common.truth.Truth.assertThat
import org.jf.dexlib2.AnnotationVisibility
import org.jf.dexlib2.iface.Annotation
import org.jf.dexlib2.immutable.ImmutableAnnotation
import org.jf.dexlib2.immutable.ImmutableAnnotationElement
import org.jf.dexlib2.immutable.reference.ImmutableFieldReference
import org.jf.dexlib2.immutable.value.ImmutableArrayEncodedValue
import org.jf.dexlib2.immutable.value.ImmutableEnumEncodedValue
import org.jf.dexlib2.immutable.value.ImmutableIntEncodedValue
import org.jf.dexlib2.immutable.value.ImmutableStringEncodedValue
import org.junit.jupiter.api.Test

internal class AnnotationExtractorTest {

    @Test
    fun `extract array of strings`() {
        val key = "x"
        val annotation: Annotation = ImmutableAnnotation(
            AnnotationVisibility.RUNTIME,
            "Lcom.avito.Whatever;",
            setOf(
                ImmutableAnnotationElement(
                    key,
                    ImmutableArrayEncodedValue(
                        ImmutableList.of(
                            ImmutableStringEncodedValue("one"),
                            ImmutableStringEncodedValue("two"),
                            ImmutableStringEncodedValue("three")
                        )
                    )
                )
            )
        )
        val annotationData = AnnotationExtractor.toAnnotationData(annotation)

        val result = annotationData.getStringArrayValue(key)

        assertThat(result).containsExactly("one", "two", "three")
    }

    @Test
    fun `extract array of ints`() {
        val key = "x"
        val annotation: Annotation = ImmutableAnnotation(
            AnnotationVisibility.RUNTIME,
            "Lcom.avito.Whatever;",
            setOf(
                ImmutableAnnotationElement(
                    key,
                    ImmutableArrayEncodedValue(
                        ImmutableList.of(
                            ImmutableIntEncodedValue(1),
                            ImmutableIntEncodedValue(2),
                            ImmutableIntEncodedValue(3)
                        )
                    )
                )
            )
        )
        val annotationData = AnnotationExtractor.toAnnotationData(annotation)

        val result = annotationData.getStringArrayValue(key)

        assertThat(result).containsExactly(1, 2, 3)
    }

    @Test
    fun `extract enum`() {
        val key = "x"
        val annotation: Annotation = ImmutableAnnotation(
            AnnotationVisibility.RUNTIME,
            "Lcom.avito.Whatever;",
            setOf(
                ImmutableAnnotationElement(
                    key,
                    ImmutableEnumEncodedValue(
                        ImmutableFieldReference("", "major", "")
                    )
                )
            )
        )
        val annotationData = AnnotationExtractor.toAnnotationData(annotation)

        val result = annotationData.getEnumValue(key)

        assertThat(result).isEqualTo("major")
    }
}
