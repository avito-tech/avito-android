package com.avito.android

import com.avito.android.clickstream.ClickStreamEventTracker
import com.avito.android.clickstream.ClickStreamSenderService
import com.avito.android.module_type.FunctionalType
import com.avito.android.module_type.ModuleType
import org.gradle.api.DefaultTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

internal abstract class CountDemoAppsTask @Inject constructor(
    objects: ObjectFactory,
) : DefaultTask() {

    @get:Input
    internal val moduleTypes: ListProperty<ModuleType> = objects.listProperty(ModuleType::class.java)

    @get:Internal
    internal abstract val clickStreamSenderService: Property<ClickStreamSenderService>

    @TaskAction
    fun generate() {
        val demoAppCount = moduleTypes.get().count { moduleType ->
            moduleType.type == FunctionalType.DemoApp
        }
        val service = clickStreamSenderService.get()
        val tracker = ClickStreamEventTracker(service)
        tracker.trackEvent(event = CountDemoAppsEvent(count = demoAppCount))
    }
}
