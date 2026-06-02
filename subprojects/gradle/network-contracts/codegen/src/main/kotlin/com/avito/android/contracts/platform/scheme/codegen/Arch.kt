package com.avito.android.contracts.platform.scheme.codegen

public sealed class OsPlatform(
    public val arch: CpuArch
) {

    public class Darwin(arch: CpuArch) : OsPlatform(arch)
    public class Linux(arch: CpuArch) : OsPlatform(arch)
    public data object Unknown : OsPlatform(CpuArch.UNKNOWN)
}

public enum class CpuArch {
    X86_64,
    ARM64,
    UNKNOWN,
    ;

    public companion object {

        public fun current(): CpuArch = when (System.getProperty("os.arch")) {
            "x86_64", "amd64" -> X86_64
            "aarch64", "arm64" -> ARM64
            else -> UNKNOWN
        }
    }
}
