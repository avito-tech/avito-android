package com.avito.android.contracts.platform.internal.analytics

import com.avito.android.Result
import com.avito.android.clickstream.ClickStreamEventTracker
import com.avito.android.clickstream.ClickStreamSenderService
import com.avito.android.clickstream.EventsTracker
import com.avito.android.clickstream.event.ClickStreamEvent
import com.avito.android.contracts.platform.analytics.ActionEnvironment
import com.avito.android.contracts.platform.analytics.toActionEnvironment
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.utils.gradle.buildEnvironment
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

public interface NetworkContractsAnalyticsService : BuildService<NetworkContractsAnalyticsService.Parameters> {

    public val tracker: EventsTracker

    public interface Parameters : BuildServiceParameters {

        public val analyticsEnable: Property<Boolean>
        public val actionEnvironment: Property<ActionEnvironment>
        public val analyticsSenderService: Property<ClickStreamSenderService>
    }

    public companion object {

        public fun provideService(
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
                                default = true
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
