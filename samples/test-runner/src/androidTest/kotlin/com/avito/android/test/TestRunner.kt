package com.avito.android.test

import android.os.Bundle
import com.avito.android.runner.InHouseInstrumentationTestRunner
import com.avito.android.runner.TestRunEnvironment
import com.avito.android.runner.annotation.resolver.AnnotationResolversBasedMetadataInjector
import com.avito.android.runner.annotation.resolver.NetworkingResolver
import com.avito.android.runner.annotation.resolver.TestMetadataAnnotationResolver
import com.avito.android.runner.annotation.resolver.TestMetadataInjector
import com.avito.android.runner.parseEnvironment
import com.avito.android.test.report.BundleArgsProvider

class TestRunner : InHouseInstrumentationTestRunner() {

    override val metadataToBundleInjector: TestMetadataInjector = AnnotationResolversBasedMetadataInjector(
        setOf(
            TestMetadataAnnotationResolver(),
            NetworkingResolver()
        )
    )

    override fun createRunnerEnvironment(arguments: Bundle): TestRunEnvironment {
        return parseEnvironment(
            argumentsProvider = BundleArgsProvider(bundle = arguments)
        )
    }
}
