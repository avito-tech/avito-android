package com.avito.instrumentation.internal

import com.avito.instrumentation.InstrumentationTestsTask
import com.avito.instrumentation.configuration.ExecutionEnvironment
import com.avito.instrumentation.configuration.KubernetesViaContext
import com.avito.instrumentation.configuration.KubernetesViaCredentials
import com.avito.instrumentation.configuration.LocalAdb
import com.avito.utils.gradle.KubernetesCredentials

internal class EnvironmentConfigurator(
    private val environment: ExecutionEnvironment
) : InstrumentationTaskConfigurator {

    override fun configure(task: InstrumentationTestsTask) {
        when (environment) {
            is KubernetesViaCredentials ->
                task.kubernetesCredentials.set(
                    KubernetesCredentials.Service(
                        token = environment.token.get(),
                        caCertData = environment.caCertData.get(),
                        url = environment.url.get(),
                        namespace = environment.namespace.get()
                    )
                )

            is KubernetesViaContext ->
                task.kubernetesCredentials.set(
                    KubernetesCredentials.Config(
                        context = environment.context.get(),
                        namespace = environment.namespace.get()
                    )
                )

            is LocalAdb ->
                task.kubernetesCredentials.set(KubernetesCredentials.Empty)

            else -> throw IllegalArgumentException(
                "Unsupported environment named: '${environment.name}'; " +
                    "with type: ${environment::class.java.canonicalName}"
            )
        }
    }
}
