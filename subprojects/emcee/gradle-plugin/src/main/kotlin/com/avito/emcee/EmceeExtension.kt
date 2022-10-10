package com.avito.emcee

import org.gradle.api.Action
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import java.time.Duration

public abstract class EmceeExtension(objects: ObjectFactory) : DevicesContainer {

    @get:Nested
    internal abstract val job: JobConfiguration

    @get:Nested
    internal abstract val artifactory: ArtifactoryConfiguration

    public abstract val retries: Property<Int>

    internal abstract val devices: ListProperty<Device>

    public abstract val testTimeout: Property<Duration>

    public abstract val queueBaseUrl: Property<String>

    public val configTestMode: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    public abstract val outputDir: DirectoryProperty

    public fun job(action: Action<JobConfiguration>) {
        action.execute(job)
    }

    public fun artifactory(action: Action<ArtifactoryConfiguration>) {
        action.execute(artifactory)
    }

    public fun devices(action: Action<DevicesContainer>) {
        action.execute(this)
    }

    override fun addDevice(sdk: Int, type: String) {
        devices.add(Device(sdk, type))
    }
}

public interface DevicesContainer {
    public fun addDevice(sdk: Int, type: String)
}
