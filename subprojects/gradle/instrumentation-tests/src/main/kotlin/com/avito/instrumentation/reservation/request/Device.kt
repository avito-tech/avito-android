package com.avito.instrumentation.reservation.request

import java.io.Serializable

sealed class Device : Serializable {

    abstract val name: String
    abstract val model: String
    abstract val api: Int
    abstract val description: String

    data class LocalEmulator(
        override val name: String,
        override val api: Int,
        override val model: String
    ) : Device() {

        override val description: String
            get() = "local-emulator-$name"

        companion object {
            @JvmStatic
            @JvmOverloads
            fun device(api: Int, model: String = "Android_SDK_built_for_x86"): LocalEmulator = LocalEmulator(
                name = api.toString(),
                model = model,
                api = api
            )
        }
    }

    class MockEmulator(
        override val name: String,
        override val model: String,
        override val api: Int
    ) : Device() {

        override val description: String = "mock-$name"

        companion object {

            @JvmStatic
            @JvmOverloads
            fun create(api: Int, model: String = "Android_SDK_built_for_x86"): MockEmulator = MockEmulator(
                name = "Mock",
                model = model,
                api = api
            )
        }
    }

    class CloudEmulator(
        override val name: String,
        override val api: Int,
        override val model: String,
        val image: String,
        val gpu: Boolean = false,
        val cpuCoresLimit: String? = null,
        val cpuCoresRequest: String? = null,
        val memoryLimit: String? = null,
        val memoryRequest: String? = null
    ) : Device() {

        override val description: String = "emulator-$name"

        companion object
    }

    sealed class Phone(
        override val name: String,
        override val model: String,
        override val api: Int,
        val proxyImage: String
    ) : Device() {

        override val description: String
            get() = "$name-$model-api-$api"

        // TODO: api aware real devices reservations
        object Pixel3Phone : Phone(
            name = "real_phone",
            model = "Pixel_3",
            api = 29,
            proxyImage = "android/device:54bb1105d2"
        )

        object SamsungS8Phone : Phone(
            name = "real_phone",
            model = "SM_G950F",
            api = 26,
            proxyImage = "android/device:54bb1105d2"
        )
    }
}
