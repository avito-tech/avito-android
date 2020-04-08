package com.avito.instrumentation.suite.filter

import com.avito.instrumentation.suite.dex.AnnotationData
import com.avito.report.model.DeviceName
import java.io.Serializable

interface TestsFilter {

    sealed class Result {
        object Included : Result()
        abstract class Excluded(val reason: String) : Result() {
            class HaveSkipSdkAnnotation(sdk: Int) : Excluded("test has SkipSdk with value sdk=$sdk")
            class DoNotHaveIncludeAnnotations(annotations: Set<String>) : Excluded("test doesn't have any of annotations=$annotations")
            class HaveExcludeAnnotations(annotations: Set<String>) : Excluded("test has any of excluded annotations=$annotations")
            abstract class BySignatures(reason: String): Excluded(reason) {
                abstract val source: Signatures.Source
            }
            class DoNotMatchIncludeSignature(
                override val source: Signatures.Source
            ) : BySignatures("test doesn't match any of signatures from source=$source")

            class MatchExcludeSignature(
                override val source: Signatures.Source
            ) : BySignatures("test has matched one of signatures from source=$source")
        }
    }

    data class Test(
        val name: String,
        val annotations: List<AnnotationData>,
        val deviceName: DeviceName,
        val api: Int
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

    fun filter(test: Test): Result
}