package com.avito.android.clickstream

import com.avito.android.Result
import com.avito.android.clickstream.api.ClickStreamEventRequest
import com.avito.android.clickstream.config.ClickStreamConfig
import com.avito.android.clickstream.config.clickStreamConfig
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

public abstract class ClickStreamSenderService : BuildService<ClickStreamSenderService.Params>,
    ClickStreamSender {

    public interface Params : BuildServiceParameters {
        public val clickStreamConfig: Property<ClickStreamConfig>
    }

    private val senderDelegate: ClickStreamSender by lazy {
        ClickStreamSenderImpl(
            config = parameters.clickStreamConfig.get()
        )
    }

    public override fun sendEvents(envelope: ClickStreamEventRequest): Result<Unit> {
        return senderDelegate.sendEvents(envelope)
    }

    public companion object {
        public fun provideClickStreamEventService(
            project: Project,
        ): Provider<ClickStreamSenderService> {
            return registerService(project)
        }

        private fun registerService(project: Project): Provider<ClickStreamSenderService> {
            return project.gradle.sharedServices.registerIfAbsent(
                ClickStreamSenderService::class.java.name,
                ClickStreamSenderService::class.java,
            ) {
                it.parameters { params ->
                    params.clickStreamConfig.set(project.clickStreamConfig)
                }
            }
        }
    }
}
