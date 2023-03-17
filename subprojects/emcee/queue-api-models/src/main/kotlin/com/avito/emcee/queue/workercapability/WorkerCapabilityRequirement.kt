package com.avito.emcee.queue.workercapability

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class WorkerCapabilityRequirement(
    val capabilityName: String,
    val constraint: WorkerCapabilityConstraint,
) {

    @JsonClass(generateAdapter = true)
    public data class WorkerCapabilityConstraint(
        val type: String,
        val value: String,
    )

    public companion object {
        public fun matching(capability: WorkerCapability): WorkerCapabilityRequirement = WorkerCapabilityRequirement(
            capabilityName = capability.name,
            constraint = WorkerCapabilityConstraint(
                type = "equal",
                value = capability.value
            )
        )
    }
}
