package com.avito.android.test.page_object

import androidx.annotation.CallSuper
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.avito.android.screen.BaseScreenChecks
import com.avito.android.screen.Screen
import com.avito.android.test.InteractionContext
import com.avito.android.test.SimpleInteractionContext

abstract class SimpleScreen : PageObject(), Screen {

    override val interactionContext: InteractionContext by lazy {
        SimpleInteractionContext(ViewMatchers.isRoot()) {
            if (checks.checkOnEachScreenInteraction) {
                checks.isScreenOpened()
            }
        }
    }

    override val checks: com.avito.android.screen.ScreenChecks =
        SimpleScreenChecks(screen = this, checkOnEachScreenInteraction = false)

    val rootElement: ViewElement by lazy {
        // rootId property is initialized after rootElement property
        element(withId(rootId))
    }

    open class SimpleScreenChecks<T : SimpleScreen>(
        screen: T,
        override val checkOnEachScreenInteraction: Boolean = false
    ) : BaseScreenChecks<T>(screen) {

        @CallSuper
        override fun screenOpenedCheck() {
            if (screen.rootId != Screen.UNKNOWN_ROOT_ID) {
                screen.rootElement.checks.exists()
                screen.rootElement.checks.isDisplayed()
            }
        }
    }
}
