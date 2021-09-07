package com.avito.android.runner.devices.internal.kubernetes

import com.avito.android.Result
import com.avito.k8s.KubernetesApi
import com.avito.k8s.model.KubePod
import com.avito.logger.Logger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@ExperimentalCoroutinesApi
internal class DeploymentPodsListener(
    private val logger: Logger,
    private val lock: Mutex,
    private val kubernetesApi: KubernetesApi,
    private val podsQueryIntervalMs: Long
) {
    suspend fun start(
        deploymentName: String,
        podsChannel: SendChannel<KubePod>
    ): Result<Unit> {
        logger.debug("Start listening devices for $deploymentName")

        var next = true
        var result: Result<Unit> = Result.Success(Unit)
        while (next) {
            lock.withLock {
                if (!podsChannel.isClosedForSend) {
                    when (val getPodsResult = kubernetesApi.getPods(deploymentName)) {
                        is Result.Success -> {
                            getPodsResult.value.forEach { pod ->
                                podsChannel.send(pod)
                            }
                            delay(podsQueryIntervalMs)
                        }
                        is Result.Failure -> {
                            next = false
                            result = getPodsResult.map { }
                            logger.critical("Error get pods", getPodsResult.throwable)
                        }
                    }
                } else {
                    next = false
                }
            }
        }
        logger.debug("listenPodsFromDeployment finished, [deploymentName=$deploymentName]")
        return result
    }
}
