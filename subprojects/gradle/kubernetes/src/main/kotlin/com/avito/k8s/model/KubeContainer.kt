package com.avito.k8s.model

import io.fabric8.kubernetes.api.model.ContainerStateWaiting
import io.fabric8.kubernetes.api.model.ContainerStatus

public class KubeContainer(private val containerStatus: ContainerStatus?) {

    public val phase: ContainerPhase
        get() {
            val state = containerStatus?.state
            val running = state?.running
            val waiting = state?.waiting
            val terminated = state?.terminated

            return when {
                waiting != null -> ContainerPhase.Waiting(waiting.describe())
                running != null -> ContainerPhase.Running
                terminated != null -> ContainerPhase.Terminated
                else -> ContainerPhase.Unknown
            }
        }

    private fun ContainerStateWaiting.describe(): String {
        return if (!message.isNullOrBlank()) {
            message
        } else {
            "Waiting"
        }
    }

    /**
     * https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle/#container-states
     */
    public sealed class ContainerPhase {

        /**
         * If a container is not in either the Running or Terminated state, it is Waiting.
         * A container in the Waiting state is still running the operations it requires in order to complete start up:
         * for example, pulling the container image from a container image registry, or applying Secret data.
         * When you use kubectl to query a Pod with a container that is Waiting,
         * you also see a Reason field to summarize why the container is in that state
         */
        public data class Waiting(val message: String) : ContainerPhase() {

            public fun hasProblemsGettingImage(): Boolean {
                return hasInvalidImageRef() || cantAccessImage()
            }

            private fun hasInvalidImageRef(): Boolean {
                return message.contains("couldn't parse image reference")
            }

            private fun cantAccessImage(): Boolean {
                return message.contains("pull access denied for")
            }
        }

        /**
         * The Running status indicates that a container is executing without issues.
         * If there was a postStart hook configured, it has already executed and finished.
         * When you use kubectl to query a Pod with a container that is Running,
         * you also see information about when the container entered the Running state
         */
        public object Running : ContainerPhase() {
            override fun toString(): String = "Running"
        }

        /**
         * A container in the Terminated state began execution and then either ran to completion or failed for some reason.
         * When you use kubectl to query a Pod with a container that is Terminated,
         * you see a reason, an exit code, and the start and finish time for that container's period of execution.
         */
        public object Terminated : ContainerPhase() {
            override fun toString(): String = "Terminated"
        }

        /**
         * Our synthetic status to handle errors
         */
        public object Unknown : ContainerPhase() {
            override fun toString(): String = "Unknown"
        }
    }
}
