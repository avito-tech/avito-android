package com.avito.android.test

import androidx.test.espresso.EspressoException
import com.avito.android.test.interceptor.ActionInterceptor
import com.avito.android.test.interceptor.AssertionInterceptor
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

public object UITestConfig {

    /**
     * A global registry for [ActionInterceptor]'s
     */
    public val actionInterceptors: MutableList<ActionInterceptor> = mutableListOf()

    /**
     * A global registry for [AssertionInterceptor]'s
     */
    public val assertionInterceptors: MutableList<AssertionInterceptor> = mutableListOf()

    /**
     * To react on every exception during waiter loop
     */
    public var onWaiterRetry: (e: Throwable) -> Unit = {}

    /**
     * Changing this value will affect all subsequent actions/checks wait frequency
     */
    public var waiterFrequencyMs: Long = 50L

    /**
     * Changing this value will affect all subsequent actions/checks wait timeout
     */
    public var waiterTimeoutMs: Long = TimeUnit.SECONDS.toMillis(2)

    public var activityLaunchTimeoutMilliseconds: Long = TimeUnit.SECONDS.toMillis(10)

    public var openNotificationTimeoutMilliseconds: Long = TimeUnit.SECONDS.toMillis(30)

    public val defaultClicksType: ClickType = ClickType.InProcessClick

    public var clicksType: ClickType = defaultClicksType

    /**
     * Works only for [ClickType.InProcessClick]
     */
    public var visualizeClicks: Boolean = true

    /**
     * Exceptions to be waited for; any unregistered exceptions will be propagated
     */
    public var waiterAllowedExceptions: Set<Class<out Any>> = setOf(
        EspressoException::class.java,
        AssertionError::class.java
    )

    /**
     * Pattern for launcher package in Android 11+ devices.
     * Hardcoded values due to package visibility restrictions:
     * https://developer.android.com/training/package-visibility/declaring
     * Also we match "com.android.fakesystemapp" because ATD emulators use it instead of launcher
     * So matching "com.android.fakesystemapp" allows us to pass custom waitForLauncher check for ATD emulators
     */
    public var deviceLauncherPackage: Pattern = Pattern.compile(".+launcher.*|com.android.fakesystemapp")

    public sealed class ClickType {
        /**
         * Use default espresso clicks and long clicks
         */
        public class EspressoClick(public val rollbackPolicy: ClickRollbackPolicy) : ClickType() {
            /**
             * Because of clicks implementation inside Espresso sometimes clicks can be interpreted
             * as long clicks. Here we have several options to handle it.
             *
             * https://stackoverflow.com/questions/32330671/android-espresso-performs-longclick-instead-of-click
             */
            public sealed class ClickRollbackPolicy {
                public data object DoNothing : ClickRollbackPolicy()
                public data object TryOneMoreClick : ClickRollbackPolicy()
                public data object Fail : ClickRollbackPolicy()
            }
        }

        /**
         * [Documentation](https://avito-tech.github.io/avito-android/docs/test_framework/internals/)
         */
        public data object InProcessClick : ClickType()
    }
}
