package com.avito.emcee.queue.workercapability

private val supportedPayload: WorkerCapability = WorkerCapability(
    name = "emcee.worker.supportedtestpayloadtype",
    value = "androidTestConfiguration"
)

public fun defaultCapabilities(): List<WorkerCapability> = listOf(
    supportedPayload
)

public fun defaultCapabilityRequirements(): List<WorkerCapabilityRequirement> = listOf(
    WorkerCapabilityRequirement.matching(supportedPayload)
)
