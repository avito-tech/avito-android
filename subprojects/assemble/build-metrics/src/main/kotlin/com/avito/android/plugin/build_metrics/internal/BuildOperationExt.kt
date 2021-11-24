package com.avito.android.plugin.build_metrics.internal

import org.gradle.internal.operations.BuildOperationDescriptor
import org.gradle.internal.operations.OperationFinishEvent

internal fun BuildOperationDescriptor.dump(): String {
    val descriptor = this
    return BuildOperationDescriptor::class.java.simpleName +
        "(name: ${descriptor.name}" +
        ", details: ${descriptor.details}" +
        ", metadata: ${descriptor.metadata}" +
        ", id: ${descriptor.id}" +
        ", parentId: ${descriptor.parentId})"
}

internal fun OperationFinishEvent.dump(): String {
    val event = this
    val duration = event.endTime - event.startTime

    return OperationFinishEvent::class.java.simpleName +
        "(result: ${event.result}" +
        ", duration: $duration (${event.startTime}:${event.endTime})" +
        ", failure: ${event.failure}" +
        ")"
}
