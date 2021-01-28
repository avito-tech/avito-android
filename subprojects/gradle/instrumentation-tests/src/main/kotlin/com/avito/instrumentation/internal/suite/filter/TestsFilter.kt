package com.avito.instrumentation.internal.suite.filter

import com.avito.android.AnnotationData
import com.avito.instrumentation.suite.filter.Filter
import com.avito.report.model.DeviceName
import com.avito.report.model.Flakiness
import java.io.Serializable

internal interface TestsFilter {

    sealed class Result {

        object Included : Result()

        abstract class Excluded(
            val byFilter: String,
            val reason: String
        ) : Result() {

            override fun toString(): String = "test has been excluded because: $reason"

            class HasSkipSdkAnnotation(name: String, sdk: Int) : Excluded(
                byFilter = name,
                reason = "test has SkipSdk with value sdk=$sdk"
            )

            class HasFlakyAnnotation(name: String, sdk: Int) : Excluded(
                byFilter = name,
                reason = "test has Flaky with value sdk=$sdk"
            )

            class DoesNotHaveIncludeAnnotations(name: String, annotations: Set<String>) : Excluded(
                byFilter = name,
                reason = "test doesn't have any of annotations=$annotations"
            )

            class HasExcludeAnnotations(name: String, annotations: Set<String>) : Excluded(
                byFilter = name,
                reason = "test has any of excluded annotations=$annotations"
            )

            abstract class BySignatures(name: String, reason: String) : Excluded(name, reason) {
                abstract val source: Signatures.Source
            }

            class DoesNotMatchIncludeSignature(name: String, override val source: Signatures.Source) : BySignatures(
                name = name,
                reason = "test doesn't match any of signatures from source=$source"
            )

            class MatchesExcludeSignature(name: String, override val source: Signatures.Source) : BySignatures(
                name = name,
                reason = "test has matched one of signatures from source=$source"
            )
        }
    }

    data class Test(
        val name: String,
        val annotations: List<AnnotationData>,
        val deviceName: DeviceName,
        val api: Int,
        val flakiness: Flakiness
    ) {
        companion object
    }

    data class Signatures(
        val source: Source,
        val signatures: Filter.Value<TestSignature>
    ) {
        enum class Source {
            Code, ImpactAnalysis, PreviousRun, Report
        }

        data class TestSignature(
            val name: String,
            val deviceName: String? = null
        ) : Serializable
    }

    val name: String

    fun filter(test: Test): Result
}
