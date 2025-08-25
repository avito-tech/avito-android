package com.avito.android.network_contracts.internal.analytics

import com.avito.android.Result
import com.avito.android.clickstream.ClickStreamEventTracker
import com.avito.android.clickstream.ClickStreamSenderService
import com.avito.android.clickstream.EventsTracker
import com.avito.android.clickstream.event.ClickStreamEvent
import com.avito.android.network_contracts.analytics.ActionEnvironment
import com.avito.android.network_contracts.analytics.toActionEnvironment
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.utils.gradle.BuildEnvironment
import com.avito.utils.gradle.buildEnvironment
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

internal interface NetworkContractsAnalyticsService : BuildService<NetworkContractsAnalyticsService.Parameters> {

    val tracker: EventsTracker

    interface Parameters : BuildServiceParameters {

        val analyticsEnable: Property<Boolean>
        val actionEnvironment: Property<ActionEnvironment>
        val analyticsSenderService: Property<ClickStreamSenderService>
    }

    companion object {

        fun provideService(
            project: Project,
        ): Provider<out NetworkContractsAnalyticsService> {
            return registerService(project)
        }

        private fun registerService(project: Project): Provider<out NetworkContractsAnalyticsService> {
            return project.gradle.sharedServices.registerIfAbsent(
                NetworkContractsAnalyticsService::class.java.name,
                NetworkContractsAnalyticsServiceImpl::class.java,
            ) {
                it.parameters { params ->
                    params.analyticsEnable
                        .set(
                            project.getBooleanProperty(
                                name = "avito.networkContracts.analytics.enabled",
                                default = project.buildEnvironment is BuildEnvironment.CI
                            )
                        )

                    params.analyticsSenderService
                        .set(ClickStreamSenderService.provideClickStreamEventService(project))

                    params.actionEnvironment
                        .set(project.buildEnvironment.toActionEnvironment())
                }
            }
        }
    }
}

internal abstract class NetworkContractsAnalyticsServiceImpl : NetworkContractsAnalyticsService {

    override val tracker: EventsTracker by lazy { createTracker() }

    private fun createTracker(): EventsTracker {
        return if (parameters.analyticsEnable.get()) {
            ClickStreamEventTracker(
                clickStreamSender = parameters.analyticsSenderService.get(),
                clickStreamEventSaturator = ::createNetworkContractsEnvelope
            )
        } else {
            NoOpTracker
        }
    }

    private fun createNetworkContractsEnvelope(): Map<String, String> {
        return mapOf(
            "dev_tool_name" to "gradle",
            "dev_tool_env" to parameters.actionEnvironment.get().value
        )
    }
}

private object NoOpTracker : EventsTracker {
    override fun trackEvent(event: ClickStreamEvent): Result<Unit> {
        return Result.Success(Unit)
    }
}
