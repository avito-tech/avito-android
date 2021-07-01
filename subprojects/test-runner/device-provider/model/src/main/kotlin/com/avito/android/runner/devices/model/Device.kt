package com.avito.instrumentation.reservation.request

import java.io.Serializable

/**
 * used in build scripts, configuring instrumentation-tests plugin
 *
 * todo separate this model and gradle plugin config
 * todo and change package after (it's build script API change for now)
 */
public sealed class Device : Serializable {

    public abstract val name: String
    public abstract val model: String
    public abstract val api: Int
    public abstract val description: String

    public data class LocalEmulator(
        override val name: String,
        override val api: Int,
        override val model: String
    ) : Device() {

        override val description: String
            get() = "local-emulator-$name"

        public companion object {
            @JvmStatic
            @JvmOverloads
            public fun device(api: Int, model: String = "Android_SDK_built_for_x86"): LocalEmulator = LocalEmulator(
                name = api.toString(),
                model = model,
                api = api
            )
        }
    }

    public class MockEmulator(
        override val name: String,
        override val model: String,
        override val api: Int
    ) : Device() {

        override val description: String = "mock-$name"

        public companion object {

            @JvmStatic
            @JvmOverloads
            public fun create(api: Int, model: String = "Android_SDK_built_for_x86"): MockEmulator = MockEmulator(
                name = "Mock",
                model = model,
                api = api
            )
        }
    }

    public data class CloudEmulator(
        override val name: String,
        override val api: Int,
        override val model: String,
        public val image: String,
        public val gpu: Boolean = false,
        public val cpuCoresLimit: String? = null,
        public val cpuCoresRequest: String? = null,
        public val memoryLimit: String? = null,
        public val memoryRequest: String? = null
    ) : Device() {

        override val description: String = "emulator-$name"

        public companion object
    }
}
