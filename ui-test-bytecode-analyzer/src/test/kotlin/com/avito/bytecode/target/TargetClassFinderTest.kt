package com.avito.bytecode.target

import com.avito.bytecode.extractTargetClasses
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class TargetClassFinderTest {

    @Test
    fun `all target classes found`() {
        val targetClasses = extractTargetClasses()

        assertThat(targetClasses).containsExactly(
            "com.example.dimorinny.example.screen.Page1",
            "com.example.dimorinny.example.screen.Page2",
            "com.example.dimorinny.example.screen.Page3FirstImplementation",
            "com.example.dimorinny.example.screen.Page3SecondImplementation",
            "com.example.dimorinny.example.screen.Page3SuperSpecialFirstImplementation",
            "com.example.dimorinny.example.screen.Page3SuperSpecialSecondImplementation",
            "com.example.dimorinny.example.screen.Page4FirstImplementation",
            "com.example.dimorinny.example.screen.Page4SecondImplementation"
        )
    }

    @Test
    fun `class is not target - when it is abstract class or interface`() {
        val targetClasses = extractTargetClasses()

        assertThat(targetClasses).containsNoneOf(
            "com.example.dimorinny.example.screen.Page3",
            "com.example.dimorinny.example.screen.Page4",
            "com.example.dimorinny.example.screen.SuperSpecialPage3"
        )
    }
}
