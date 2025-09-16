package com.avito.k8s.model.toleration

import java.io.Serializable

public sealed class TolerationOperator(public val value: String) : Serializable {
    public data object Equal : TolerationOperator("Equal") {
        public fun readResolve(): Any = Equal
    }

    public data object Exists : TolerationOperator("Exists") {
        public fun readResolve(): Any = Exists
    }

    public companion object {
        public fun fromValue(value: String): TolerationOperator {
            return when (value) {
                "Equal" -> Equal
                "Exists" -> Exists
                else -> throw IllegalArgumentException("Invalid toleration operator: $value")
            }
        }
    }
}
