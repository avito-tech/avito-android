package com.avito.k8s.model

import io.fabric8.kubernetes.api.model.ContainerStatus
import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.api.model.PodStatus
import io.fabric8.kubernetes.client.LocalPortForward

/**
 * Wrapper to deal with nullability and abstract from fabric8io API changes
 */
public class KubePod(private val pod: Pod, public val portForward: LocalPortForward? = null) {

    private val podStatus: PodStatus?
        get() = pod.status

    private val containerStatus: ContainerStatus?
        get() = podStatus?.containerStatuses?.firstOrNull()

    public val node: String?
        get() = pod.spec?.nodeName

    public val name: String
        get() = pod.metadata.name

    public val ip: String?
        get() = podStatus?.podIP

    public val phase: PodPhase
        get() {
            val status = podStatus
            return if (status == null) {
                PodPhase.Unknown
            } else {
                when (status.phase) {
                    "Running" -> PodPhase.Running
                    "Pending" -> PodPhase.Pending(status.describe())
                    "Failed" -> PodPhase.Failed(status.describe())
                    "Succeeded" -> PodPhase.Succeeded
                    else -> PodPhase.Unknown
                }
            }
        }

    /**
     * We have one emulator container per pod
     */
    public val container: KubeContainer
        get() = KubeContainer(containerStatus)

    override fun toString(): String {
        return "Pod $name [ip=$ip; node=$node; pod=$phase; container=${container.phase}]"
    }

    private fun PodStatus.describe(): String {
        return when {
            !message.isNullOrBlank() && !reason.isNullOrBlank() -> "$message; reason=$reason"
            !message.isNullOrBlank() -> message
            !reason.isNullOrBlank() -> reason
            else -> {
                val lastConditionMessage = conditions.sortedByDescending { it.lastTransitionTime }
                    .mapNotNull { it.message }
                    .firstOrNull()

                if (!lastConditionMessage.isNullOrBlank()) {
                    lastConditionMessage
                } else {
                    "Unknown"
                }
            }
        }
    }

    /**
     * https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle/
     */
    public sealed class PodPhase {

        /**
         * The Pod has been bound to a node, and all of the containers have been created.
         * At least one container is still running, or is in the process of starting or restarting.
         */
        public object Running : PodPhase() {
            override fun toString(): String = "Running"
        }

        /**
         * The Pod has been accepted by the Kubernetes cluster,
         * but one or more of the containers has not been set up and made ready to run.
         * This includes time a Pod spends waiting to be scheduled
         * as well as the time spent downloading container images over the network.
         */
        public data class Pending(val message: String) : PodPhase()

        /**
         * All containers in the Pod have terminated, and at least one container has terminated in failure.
         * That is, the container either exited with non-zero status or was terminated by the system.
         */
        public data class Failed(val message: String) : PodPhase()

        /**
         * All containers in the Pod have terminated in success, and will not be restarted.
         */
        public object Succeeded : PodPhase() {
            override fun toString(): String = "Succeeded"
        }

        /**
         * For some reason the state of the Pod could not be obtained.
         * This phase typically occurs due to an error in communicating with the node where the Pod should be running.
         */
        public object Unknown : PodPhase() {
            override fun toString(): String = "Unknown"
        }
    }

    public companion object
}
