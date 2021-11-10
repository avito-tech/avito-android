package com.avito.android.runner.annotation.validation

import com.avito.android.api.AbstractMockApiRule
import com.avito.android.mock.MockWebServerApiRule
import com.avito.android.runner.annotation.resolver.NetworkingResolver
import com.avito.android.runner.annotation.resolver.NetworkingType
import com.avito.android.runner.annotation.resolver.TestKindExtractor
import com.avito.android.runner.annotation.resolver.TestMetadataResolver.Resolution
import com.avito.android.runner.annotation.resolver.TestMethodOrClass
import com.avito.android.test.annotations.E2ETest
import com.avito.report.model.Kind

class NetworkIsMockedValidator : TestMetadataValidator {

    override fun validate(test: TestMethodOrClass) {
        @Suppress("NON_EXHAUSTIVE_WHEN")
        when (TestKindExtractor.extract(test)) {
            Kind.UI_COMPONENT -> validateNoRealNetwork(test.testClass)
            else -> {
                // do nothing
            }
        }
    }

    private fun validateNoRealNetwork(testClass: Class<*>) {
        val networkingType = when (val resolution = NetworkingResolver().resolver(testClass)) {
            is Resolution.ReplaceSerializable -> resolution.replacement
            else -> throw IllegalStateException("Unexpected network type resolution $resolution in $testClass")
        }

        if (networkingType == NetworkingType.REAL) {
            throw IllegalStateException(
                "Component test ${testClass.canonicalName} uses a real network. " +
                    "It makes test unstable. There are two options:\n" +
                    "- Mock network by ${MockWebServerApiRule::class.java.simpleName} " +
                    "or ${AbstractMockApiRule::class.java.simpleName} implementations\n" +
                    "- Make test @${E2ETest::class.java.simpleName}"
            )
        }
    }
}
