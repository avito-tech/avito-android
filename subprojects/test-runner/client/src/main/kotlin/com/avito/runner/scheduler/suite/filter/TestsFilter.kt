package com.avito.runner.scheduler.suite.filter

import com.avito.android.AnnotationData
import com.avito.report.model.DeviceName
import com.avito.report.model.Flakiness
import java.io.Serializable

public interface TestsFilter {

    public sealed class Result {

        public object Included : Result()

        public abstract class Excluded(
            public val byFilter: String,
            public val reason: String
        ) : Result() {

            override fun toString(): String = "test has been excluded because: $reason"

            public class HasSkipSdkAnnotation(name: String, sdk: Int) : Excluded(
                byFilter = name,
                reason = "test has SkipSdk with value sdk=$sdk"
            )

            public class HasFlakyAnnotation(name: String, sdk: Int) : Excluded(
                byFilter = name,
                reason = "test has Flaky with value sdk=$sdk"
            )

            public class DoesNotHaveIncludeAnnotations(name: String, annotations: Set<String>) : Excluded(
                byFilter = name,
                reason = "test doesn't have any of annotations=$annotations"
            )

            public class HasExcludeAnnotations(name: String, annotations: Set<String>) : Excluded(
                byFilter = name,
                reason = "test has any of excluded annotations=$annotations"
            )

            public abstract class BySignatures(name: String, reason: String) : Excluded(name, reason) {
                public abstract val source: Signatures.Source
            }

            public class DoesNotMatchIncludeSignature(name: String, override val source: Signatures.Source) :
                BySignatures(
                    name = name,
                    reason = "test doesn't match any of signatures from source=$source"
                )

            public class MatchesExcludeSignature(name: String, override val source: Signatures.Source) : BySignatures(
                name = name,
                reason = "test has matched one of signatures from source=$source"
            )
        }
    }

    public data class Test(
        val name: String,
        val annotations: List<AnnotationData>,
        val deviceName: DeviceName,
        val api: Int,
        val flakiness: Flakiness
    ) {
        internal companion object
    }

    public data class Signatures(
        val source: Source,
        val signatures: Filter.Value<TestSignature>
    ) {
        public enum class Source {
            Code, ImpactAnalysis, PreviousRun, Report
        }

        public data class TestSignature(
            val name: String,
            val deviceName: String? = null
        ) : Serializable
    }

    public val name: String

    public fun filter(test: Test): Result
}
