package com.avito.android.test.page_object

import android.view.View
import androidx.test.espresso.action.PrecisionDescriber
import androidx.test.espresso.action.SwipeDirection
import androidx.test.espresso.action.Swiper
import com.avito.android.test.InteractionContext
import com.avito.android.test.SimpleInteractionContext
import com.avito.android.test.action.Actions
import com.avito.android.test.action.ActionsImpl
import com.avito.android.test.checks.Checks
import com.avito.android.test.checks.ChecksImpl
import com.avito.android.test.matcher.NoViewMatcher
import org.hamcrest.Matcher

open class ViewElement : PageObjectElement, Actions {

    override val matcher: Matcher<View>
    override val interactionContext: InteractionContext
    override val actions: Actions
    override val checks: Checks

    /**
     * "Use constructor with interaction context. " +
     * "Otherwise, you can get actions and checks in a wrong screen or window." +
     * "See [PageObject.element] to create an instance with correct interaction context inside page objects." +
     * "For a custom class use `ViewElement(interactionContext)`"
     * TODO: make this constructor private and remove matcher
     */
    constructor(
        matcher: Matcher<View>,
        interactionContext: InteractionContext = SimpleInteractionContext(matcher),
        actions: Actions = ActionsImpl(interactionContext),
        checks: Checks = ChecksImpl(interactionContext)
    ) {
        this.matcher = matcher
        this.interactionContext = interactionContext
        this.actions = actions
        this.checks = checks
    }

    @Suppress("DEPRECATION")
    constructor(interactionContext: InteractionContext) :
        this(
            NoViewMatcher(),
            interactionContext,
            ActionsImpl(interactionContext),
            ChecksImpl(interactionContext)
        )

    @Suppress("DEPRECATION")
    constructor(interactionContext: InteractionContext, checks: Checks) :
        this(NoViewMatcher(), interactionContext, ActionsImpl(interactionContext), checks)

    override fun click() = actions.click()

    override fun longClick() = actions.longClick()

    override fun scrollTo() = actions.scrollTo()

    override fun swipe(direction: SwipeDirection, speed: Swiper, precision: PrecisionDescriber) =
        actions.swipe(direction, speed, precision)

    override fun read(allowBlank: Boolean) = actions.read(allowBlank)
}
