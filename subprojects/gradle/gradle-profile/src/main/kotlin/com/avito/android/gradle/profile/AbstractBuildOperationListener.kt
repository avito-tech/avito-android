package com.avito.android.gradle.profile

import org.gradle.internal.operations.BuildOperationDescriptor
import org.gradle.internal.operations.BuildOperationListener
import org.gradle.internal.operations.OperationFinishEvent
import org.gradle.internal.operations.OperationIdentifier
import org.gradle.internal.operations.OperationProgressEvent
import org.gradle.internal.operations.OperationStartEvent

/**
 * Not thread safe, see BuildOperationListener documentation
 *
 * Events are hierarchically organized by id <--> parentId relationships
 */
public abstract class AbstractBuildOperationListener : BuildOperationListener {

    override fun started(descriptor: BuildOperationDescriptor, event: OperationStartEvent) {
        // no-op
    }

    override fun progress(identifier: OperationIdentifier, event: OperationProgressEvent) {
        // no-op
    }

    override fun finished(descriptor: BuildOperationDescriptor, event: OperationFinishEvent) {
        // no-op
    }
}
