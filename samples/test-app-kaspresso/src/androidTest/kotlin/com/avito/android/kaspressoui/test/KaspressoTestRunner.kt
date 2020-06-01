package com.avito.android.kaspressoui.test

import android.content.Intent
import android.os.Bundle
import com.avito.android.runner.InHouseInstrumentationTestRunner
import com.avito.android.runner.TestRunEnvironment
import com.avito.android.runner.annotation.resolver.AnnotationResolversBasedMetadataInjector
import com.avito.android.runner.annotation.resolver.NetworkingResolver
import com.avito.android.runner.annotation.resolver.TestMetadataAnnotationResolver
import com.avito.android.runner.annotation.resolver.TestMetadataInjector
import com.avito.android.runner.provideEnvironment
import com.avito.android.test.report.BundleArgsProvider

class KaspressoTestRunner : InHouseInstrumentationTestRunner() {

    override fun createRunnerEnvironment(arguments: Bundle): TestRunEnvironment =
        provideEnvironment(
//            todo эти параметры должны быть не обязательными
            apiUrlParameterKey = "unnecessaryUrl",
            mockWebServerUrl = "localhost",
            argumentsProvider = BundleArgsProvider(bundle = arguments)
        )


    override val metadataToBundleInjector: TestMetadataInjector = AnnotationResolversBasedMetadataInjector(
        setOf(
            TestMetadataAnnotationResolver(),
            NetworkingResolver()
        )
    )

    override fun onStart() {
        super.onStart()
        targetContext.sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
    }
}
