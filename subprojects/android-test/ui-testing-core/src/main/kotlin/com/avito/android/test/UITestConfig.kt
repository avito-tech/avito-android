package com.avito.android.test

import androidx.test.espresso.EspressoException
import com.avito.android.test.interceptor.ActionInterceptor
import com.avito.android.test.interceptor.AssertionInterceptor
import java.util.concurrent.TimeUnit

object UITestConfig {

    /**
     * A global registry for [ActionInterceptor]'s
     */
    val actionInterceptors = mutableListOf<ActionInterceptor>()

    /**
     * A global registry for [AssertionInterceptor]'s
     */
    val assertionInterceptors = mutableListOf<AssertionInterceptor>()

    /**
     * To react on every exception during waiter loop
     */
    var onWaiterRetry: (e: Throwable) -> Unit = {}

    /**
     * Changing this value will affect all subsequent actions/checks wait frequency
     */
    var waiterFrequencyMs: Long = 50L

    /**
     * Changing this value will affect all subsequent actions/checks wait timeout
     */
    var waiterTimeoutMs: Long = TimeUnit.SECONDS.toMillis(2)

    var activityLaunchTimeoutMilliseconds: Long = TimeUnit.SECONDS.toMillis(10)

    var openNotificationTimeoutMilliseconds: Long = TimeUnit.SECONDS.toMillis(30)

    val defaultClicksType: ClickType = ClickType.InProcessClick

    var clicksType: ClickType = defaultClicksType

    /**
     * Exceptions to be waited for; any unregistered exceptions will be propagated
     */
    var waiterAllowedExceptions = setOf(
        EspressoException::class.java,
        AssertionError::class.java
    )

    sealed class ClickType {
        /**
         * Use default espresso clicks and long clicks
         */
        class EspressoClick(val rollbackPolicy: ClickRollbackPolicy) : ClickType() {
            /**
             * Because of clicks implementation inside Espresso sometimes clicks can be interpreted
             * as long clicks. Here we have several options to handle it.
             *
             * https://stackoverflow.com/questions/32330671/android-espresso-performs-longclick-instead-of-click
             */
            sealed class ClickRollbackPolicy {
                object DoNothing : ClickRollbackPolicy()
                object TryOneMoreClick : ClickRollbackPolicy()
                object Fail : ClickRollbackPolicy()
            }
        }

        /**
         * [Documentation](https://avito-tech.github.io/avito-android/test/inprocessclick/)
         */
        object InProcessClick : ClickType()
    }
}
