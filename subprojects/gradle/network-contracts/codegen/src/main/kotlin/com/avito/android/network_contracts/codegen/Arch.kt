package com.avito.android.network_contracts.codegen

internal sealed class Arch(val rawValue: String, val binarySuffix: String) {

    @Suppress("ClassName")
    object X86_64 : Arch("x86_64", "darwin_amd64")

    object Arm64 : Arch("arm64", "darwin_arm64")

    object LinuxAmd64 : Arch("x86_64", "linux_amd64")

    class Unknown(name: String) : Arch(name, "")

    companion object {
        fun getArch(value: String) = when (value) {
            X86_64.rawValue -> X86_64
            Arm64.rawValue -> Arm64
            LinuxAmd64.rawValue -> LinuxAmd64
            else -> Unknown(value)
        }
    }
}
