package com.avito.android.kaspressoui.test

import android.content.Intent
import android.os.Bundle
import com.avito.android.runner.InHouseInstrumentationTestRunner
import com.avito.android.runner.TestRunEnvironment
import com.avito.android.runner.annotation.resolver.AnnotationResolversBasedMetadataInjector
import com.avito.android.runner.annotation.resolver.TestMetadataInjector

class KaspressoTestRunner : InHouseInstrumentationTestRunner() {

    override fun createRunnerEnvironment(arguments: Bundle): TestRunEnvironment =
        TestRunEnvironment.OrchestratorFakeRunEnvironment

    override val metadataToBundleInjector: TestMetadataInjector = AnnotationResolversBasedMetadataInjector(emptySet())

    override fun onStart() {
        super.onStart()
        targetContext.sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
    }
}
