package com.avito.impact.configuration

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory

class BranchesSpec {

    @TestFactory
    fun isBranchProtected_fun(): List<DynamicTest> = listOf(
        Data("release/28.0", setOf("release/*"), true),
        Data("develop", setOf("master"), false),
        Data("master", setOf("master"), true),
        Data("develop", setOf("master", "release/*"), false),
        Data("develop", setOf(), false)
    )
        .map { (targetBranch, skipOptimizationOnBranchPattern, isSkipAllowed) ->
            dynamicTest("target branch = $targetBranch and protectedBranches = $skipOptimizationOnBranchPattern") {
                val result = isBranchProtected(targetBranch, skipOptimizationOnBranchPattern)
                assertThat(result).isEqualTo(isSkipAllowed)
            }
        }
}

private data class Data(
    val branch: String,
    val protectedBranches: Set<String>,
    val isProtected: Boolean
)
