package com.avito.android.test.app.core

import android.content.Intent
import android.os.Bundle
import androidx.test.espresso.IdlingPolicies
import com.avito.android.runner.InHouseInstrumentationTestRunner
import com.avito.android.runner.TestRunEnvironment
import com.avito.android.runner.annotation.resolver.AnnotationResolversBasedMetadataInjector
import com.avito.android.runner.annotation.resolver.TestMetadataAnnotationResolver
import com.avito.android.runner.annotation.resolver.TestMetadataInjector
import com.avito.android.runner.parseEnvironment
import com.avito.android.test.report.BundleArgsProvider
import java.util.concurrent.TimeUnit

class TestAppRunner : InHouseInstrumentationTestRunner() {

    override val metadataToBundleInjector: TestMetadataInjector = AnnotationResolversBasedMetadataInjector(
        setOf(
            TestMetadataAnnotationResolver(),
        )
    )

    override fun createRunnerEnvironment(arguments: Bundle): TestRunEnvironment {
        return parseEnvironment(
            argumentsProvider = BundleArgsProvider(bundle = arguments)
        )
    }

    override fun onStart() {
        super.onStart()
        IdlingPolicies.setMasterPolicyTimeout(10, TimeUnit.SECONDS)
        IdlingPolicies.setIdlingResourceTimeout(10, TimeUnit.SECONDS)
        @Suppress("DEPRECATION")
        targetContext.sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
    }
}
