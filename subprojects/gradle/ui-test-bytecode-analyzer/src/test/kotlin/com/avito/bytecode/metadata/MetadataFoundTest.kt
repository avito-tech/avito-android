package com.avito.bytecode.metadata

import com.avito.bytecode.extractMetadata
import com.avito.bytecode.metadata.IdFieldExtractor.ScreenToId
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class MetadataFoundTest {

    @Test
    fun `metadata found on target classes`() {
        val metadata: Set<ScreenToId> = extractMetadata()

        assertThat(metadata).containsAtLeast(
            ScreenToId(
                "com.example.dimorinny.example.screen.Page1",
                "1"
            ),
            ScreenToId(
                "com.example.dimorinny.example.screen.Page2",
                "2"
            ),
            ScreenToId(
                "com.example.dimorinny.example.screen.Page3FirstImplementation",
                "31"
            ),
            ScreenToId(
                "com.example.dimorinny.example.screen.Page3SecondImplementation",
                "32"
            ),
            ScreenToId(
                "com.example.dimorinny.example.screen.Page3SuperSpecialFirstImplementation",
                "-1"
            ),
            ScreenToId(
                "com.example.dimorinny.example.screen.Page3SuperSpecialSecondImplementation",
                "332"
            )
        )
    }

    @Test
    fun `metadata found on target classes from abstract class`() {
        val metadata: Set<ScreenToId> = extractMetadata()

        assertThat(metadata).containsAtLeast(
            ScreenToId("com.example.dimorinny.example.screen.Page4FirstImplementation", "4"),
            ScreenToId("com.example.dimorinny.example.screen.Page4SecondImplementation", "4")
        )
    }
}
