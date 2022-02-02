package com.avito.android.runner.devices.internal.kubernetes

import com.avito.android.runner.devices.internal.ReservationClient
import com.avito.android.runner.devices.model.ReservationData
import com.avito.k8s.KubernetesApi
import com.avito.k8s.model.KubePod
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.runner.service.worker.device.DeviceCoordinate
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

/**
 * You should know that canceling jobs manually or via throwing exception
 * will cancel whole parent job and all consuming channels.
 *
 * In [KubernetesReservationClient] case podsChannel will close automatically when [claim] job failed or canceled
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class KubernetesReservationClient(
    private val claimer: KubernetesReservationClaimer,
    private val reservationReleaser: KubernetesReservationReleaser,
    private val kubernetesReservationListener: KubernetesReservationListener,
    private val kubernetesApi: KubernetesApi,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val lock: Mutex,
    loggerFactory: LoggerFactory,
) : ReservationClient {

    private val logger = loggerFactory.create<KubernetesReservationClient>()
    private var state: State = State.Idling

    override suspend fun claim(
        reservations: Collection<ReservationData>
    ): ReservationClient.ClaimResult {
        require(reservations.isNotEmpty()) {
            "Must have at least one reservation but empty"
        }
        return lock.withLock {
            if (state !is State.Idling) {
                throw IllegalStateException("Unable claim reservation. State is already started")
            }
            kubernetesReservationListener.onClaim(reservations)
            // TODO close serialsChannel
            val serialsChannel = Channel<DeviceCoordinate>(Channel.UNLIMITED)
            with(CoroutineScope(coroutineContext + dispatcher)) {
                launch(CoroutineName("main-reservation")) {
                    val podsChannel = Channel<KubePod>(Channel.UNLIMITED)
                    val deploymentsChannel = Channel<String>(reservations.size)
                    state = State.Reserving(pods = podsChannel, deployments = deploymentsChannel)
                    claimer.claim(reservations, serialsChannel, podsChannel, deploymentsChannel)
                }
            }

            ReservationClient.ClaimResult(
                deviceCoordinates = serialsChannel
            )
        }
    }

    override suspend fun remove(podName: String) {
        withContext(CoroutineName("delete-pod-$podName") + dispatcher) {
            kubernetesReservationListener.onPodRemoved()
            kubernetesApi.deletePod(podName)
        }
    }

    override suspend fun removeDeployment(name: String) {
        reservationReleaser.releaseDeployment(name)
    }

    override suspend fun release() = withContext(dispatcher) {
        lock.withLock {
            val state = state
            if (state !is State.Reserving) {
                // TODO: check on client side beforehand
                // TODO this leads to deployment leak
                throw IllegalStateException("Unable to stop reservation job. Hasn't started yet")
            } else {
                reservationReleaser.release(state.pods, state.deployments)
                kubernetesReservationListener.onRelease()
                this@KubernetesReservationClient.state = State.Idling
            }
        }
        logger.debug("release finished")
    }

    private sealed class State {
        class Reserving(
            val pods: Channel<KubePod>,
            val deployments: Channel<String>
        ) : State()

        object Idling : State()
    }

    companion object
}
