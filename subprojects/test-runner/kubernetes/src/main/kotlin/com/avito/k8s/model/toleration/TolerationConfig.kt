package com.avito.k8s.model.toleration

import java.io.Serializable

public data class TolerationConfig(
    public val key: String,
    public val operator: TolerationOperator,
    public val value: String, // TODO: (RM) value is not required for Exists operator, make it optional
    public val effect: TolerationEffect
) : Serializable
