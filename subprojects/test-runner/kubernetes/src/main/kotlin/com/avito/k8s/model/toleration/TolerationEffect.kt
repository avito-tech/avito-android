package com.avito.k8s.model.toleration

import java.io.Serializable

public sealed class TolerationEffect(public val value: String) : Serializable {
    public data object NoSchedule : TolerationEffect("NoSchedule") {
        public fun readResolve(): Any = NoSchedule
    }

    public data object PreferNoSchedule : TolerationEffect("PreferNoSchedule") {
        public fun readResolve(): Any = PreferNoSchedule
    }

    public data object NoExecute : TolerationEffect("NoExecute") {
        public fun readResolve(): Any = NoExecute
    }

    public companion object {
        public fun fromValue(value: String): TolerationEffect {
            return when (value) {
                "NoSchedule" -> NoSchedule
                "PreferNoSchedule" -> PreferNoSchedule
                "NoExecute" -> NoExecute
                else -> throw IllegalArgumentException("Invalid toleration effect value: $value")
            }
        }
    }
}
