package com.avito.android

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class SimpleProblemBuilderTest {

    @Test
    fun `simple problem - to exception - showcase`() {
        val problem = build<SimpleProblem>(
            SimpleProblemBuilder.newBuilder(
                severity = Severity.CRITICAL,
                context = ClassContext(
                    className = "SimpleProblemBuilderTest",
                    whatsGoingOn = "doing a test"
                )
            )
                .because { "the reason is" }
                .documentedAt { "http://documentation" }
                .withShortDescription { "something went wrong" }
                .withLongDescription { "something really went wrong" }
                .addSolution { builder -> builder.withShortDescription { "try to turn off and on again" } }
                .addSolution { builder -> builder.withShortDescription { "is it really a problem?" } }
        )

        val result: RuntimeException = problem.asRuntimeException()

        assertThat(result.message).isEqualTo(
            """
    A problem happened: something went wrong
    
    Where? : SimpleProblemBuilderTest doing a test
    
    Why? : the reason is
    
    Details: something really went wrong
    
    Possible solutions:
        - try to turn off and on again
        - is it really a problem?
    
    You can learn more about this problem at http://documentation
        """.trimIndent()
        )
    }
}
