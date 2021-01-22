package com.avito.android.test.rxidler

import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import com.squareup.rx3.idler.Rx3Idler
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.functions.Function
import io.reactivex.rxjava3.functions.Supplier
import io.reactivex.rxjava3.plugins.RxJavaPlugins

/**
 * Motivation https://github.com/square/RxIdler/issues/34
 */
object RxIdlingRegistry {

    private var initialized = false
    private var registered = false

    private val registry = mutableListOf<IdlingResource>()

    /**
     * Must be called one time before Application will be created
     * read https://github.com/square/RxIdler#usage
     */
    fun initialize() {
        synchronized(RxIdlingRegistry) {
            require(!initialized) {
                "Already have been initialized"
            }
            initializeInternal()
            initialized = true
            registered = true
        }
    }

    /**
     * Enable RX Idling
     * Should be called only after [disable] have already been called
     *
     * [RxIdlingRegistry] is enabled by default after [initialize]
     */
    fun enable() {
        synchronized(RxIdlingRegistry) {
            require(initialized) {
                "Must be initialized"
            }
            require(!registered) {
                "Expect registered = false"
            }
            IdlingRegistry.getInstance().register(*registry.toTypedArray())
            registered = true
        }
    }

    /**
     * Disable RX Idling
     * read [RxIdlingRegistry] motivation
     *
     * you should prefer to use [withDisabled] because you could forgot to enable back
     */
    fun disable() {
        synchronized(RxIdlingRegistry) {
            require(initialized) {
                "Expect initialized = true"
            }
            require(registered) {
                "Expect registered = true"
            }
            IdlingRegistry.getInstance().unregister(*registry.toTypedArray())
            registered = false
        }
    }

    /**
     * Run [action] without RX Idling
     */
    fun withDisabled(action: () -> Unit) {
        synchronized(RxIdlingRegistry) {
            val wasRegistered = registered
            if (wasRegistered) {
                disable()
            }
            action()
            if (wasRegistered) {
                enable()
            }
        }
    }

    private fun initializeInternal() {
        RxJavaPlugins.setInitComputationSchedulerHandler(createInitSchedulerHandler("RxJava 3.x Computation Scheduler"))
        RxJavaPlugins.setInitIoSchedulerHandler(createInitSchedulerHandler("RxJava 3.x IO Scheduler"))
        RxJavaPlugins.setInitNewThreadSchedulerHandler(createInitSchedulerHandler("RxJava 3.x NewThread Scheduler"))
        RxJavaPlugins.setInitSingleSchedulerHandler(createInitSchedulerHandler("RxJava 3.x Single Scheduler"))
    }

    private fun createInitSchedulerHandler(name: String): Function<Supplier<Scheduler>, Scheduler> {
        return Function { scheduler ->
            synchronized(RxIdlingRegistry) {
                val idlingScheduler = Rx3Idler.wrap(scheduler.get(), name)
                registry.add(idlingScheduler)
                // This state could be reached if client disabled RxIdlingRegistry before scheduler actually initialized
                if (registered) {
                    IdlingRegistry.getInstance().register(idlingScheduler)
                }
                idlingScheduler
            }
        }
    }
}
