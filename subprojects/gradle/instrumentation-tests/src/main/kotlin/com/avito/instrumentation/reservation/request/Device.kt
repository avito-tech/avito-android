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

    class CloudEmulator(
        override val name: String,
        override val api: Int,
        override val model: String,
        val image: String,
        val gpu: Boolean = false,
        val cpuCoresLimit: String,
        val cpuCoresRequest: String,
        val memoryLimit: String = "3.5Gi"
    ) : Device() {

        override val description: String = "emulator-$name"
    }

    sealed class Emulator(
        override val name: String,
        override val api: Int,
        override val model: String = "Android_SDK_built_for_x86",
        val image: String,
        val gpu: Boolean = false,
        val cpuCoresLimit: String,
        val cpuCoresRequest: String = cpuCoresLimit
    ) : Device() {

        override val description: String
            get() = "emulator-$name"

        private object Image {
            const val emulator_22 = "android/emulator-22:116d6ed6c6"
            const val emulator_23 = "android/emulator-23:33578ce220"
            const val emulator_24 = "android/emulator-24:46b6a74473"
            const val emulator_27 = "android/emulator-27:1c36e79d44"
            const val emulator_28 = "android/emulator-28:37ac40d0fa"
        }

        object Emulator22 : Emulator(
            api = 22,
            image = Image.emulator_22,
            name = "phone_22",
            gpu = false,
            cpuCoresLimit = "1.3",
            cpuCoresRequest = "1"
        )

        object Emulator23 : Emulator(
            api = 23,
            image = Image.emulator_23,
            name = "phone_23",
            gpu = false,
            cpuCoresLimit = "1.3",
            cpuCoresRequest = "1"
        )

        object Emulator24 : Emulator(
            api = 24,
            image = Image.emulator_24,
            name = "phone_24",
            gpu = false,
            cpuCoresLimit = "1.3",
            cpuCoresRequest = "1"
        )

        object Emulator24Cores2 : Emulator(
            api = 24,
            image = Image.emulator_24,
            name = "phone_24_cores_2",
            gpu = false,
            cpuCoresLimit = "2",
            cpuCoresRequest = "1.5"
        )

        object Emulator27 : Emulator(
            api = 27,
            image = Image.emulator_27,
            name = "phone_27",
            gpu = false,
            cpuCoresLimit = "1.3",
            cpuCoresRequest = "1"
        )

        object Emulator28 : Emulator(
            model = "Android_SDK_built_for_x86_64",
            api = 28,
            image = Image.emulator_28,
            name = "phone_28",
            gpu = false,
            cpuCoresLimit = "1.3",
            cpuCoresRequest = "1"
        )

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
