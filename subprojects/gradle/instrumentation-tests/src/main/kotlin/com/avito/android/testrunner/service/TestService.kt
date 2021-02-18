package com.avito.android.testrunner.service

import com.avito.android.testrunner.service.TestService.Params
import com.avito.utils.gradle.KubernetesCredentials
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

@Suppress("UnstableApiUsage")
public abstract class TestService : BuildService<Params>, AutoCloseable {

    public interface Params : BuildServiceParameters {

        public val kubernetesCredentials: Property<KubernetesCredentials>
    }

    internal interface TestServiceProgressListener {

        fun onDelayed()
    }

    internal sealed class TestServiceRunResult {

        data class Success(val report: String) : TestServiceRunResult()

        data class Error(val report: String) : TestServiceRunResult()
    }

    init {
    }

    internal fun runTests(): TestServiceRunResult {
        Thread.sleep(5000)
        return TestServiceRunResult.Success(report = "All is good!")
    }

    override fun close() {
        // delete deployments
    }
}
