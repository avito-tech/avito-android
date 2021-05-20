package com.avito.android

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class ProblemTest {

    @Test
    fun `problem - as plain text - showcase`() {
        val problem = Problem(
            shortDescription = "something went wrong",
            context = "ProblemKtTest doing a test",
            because = "the reason is",
            possibleSolutions = listOf(
                "try to turn off and on again",
                "is it really a problem?"
            ),
            documentedAt = "http://documentation",
            throwable = RuntimeException("some exception message")
        )

        val result = problem.asPlainText()

        assertThat(result).isEqualTo(
            """
    |something went wrong
    |Where : ProblemKtTest doing a test
    |Why? : the reason is
    |Possible solutions:
    | - try to turn off and on again
    | - is it really a problem?
    |You can learn more about this problem at http://documentation
    |Cause exception message: some exception message
    |""".trimMargin()
        )
    }
}
