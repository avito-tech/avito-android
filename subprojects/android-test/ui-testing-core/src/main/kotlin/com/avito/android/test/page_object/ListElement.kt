package com.avito.android.test.page_object

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.CoordinatesProvider
import androidx.test.espresso.action.GeneralLocation
import androidx.test.espresso.action.PrecisionDescriber
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Swipe
import androidx.test.espresso.action.SwipeDirection
import androidx.test.espresso.action.SwipeDirections.BOTTOM_TO_TOP
import androidx.test.espresso.action.SwipeDirections.LEFT_TO_RIGHT
import androidx.test.espresso.action.SwipeDirections.RIGHT_TO_LEFT
import androidx.test.espresso.action.SwipeDirections.TOP_TO_BOTTOM
import androidx.test.espresso.action.Swiper
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import com.avito.android.test.InteractionContext
import com.avito.android.test.RecyclerViewInteractionContext
import com.avito.android.test.SimpleInteractionContext
import com.avito.android.test.action.Actions
import com.avito.android.test.action.ActionsDriver
import com.avito.android.test.action.ActionsImpl
import com.avito.android.test.checks.Checks
import com.avito.android.test.checks.ChecksDriver
import com.avito.android.test.checks.ChecksImpl
import com.avito.android.test.espresso.EspressoActions
import com.avito.android.test.espresso.action.RecyclerSpanCountAction
import com.avito.android.test.espresso.action.RecyclerViewHorizontalOffsetAction
import com.avito.android.test.espresso.action.RecyclerViewItemsCountAction
import com.avito.android.test.espresso.action.RecyclerViewVerticalOffsetAction
import com.avito.android.test.espresso.action.ViewGetTranslationYAction
import com.avito.android.test.espresso.action.recycler.SmoothScrollToPositionViewAction
import com.avito.android.test.espresso.action.recycler.ViewActionOnItemAtPosition
import com.avito.android.test.espresso.action.recycler.actionOnHolderItem
import com.avito.android.test.espresso.action.recycler.scrollToElementInsideRecyclerViewItem
import com.avito.android.test.matcher.RecyclerViewMatcher
import com.avito.android.test.matcher.ViewGroupMatcher
import com.forkingcode.espresso.contrib.DescendantViewActions
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.greaterThan
import org.hamcrest.TypeSafeMatcher

open class ListElement(interactionContext: InteractionContext) : ViewElement(interactionContext) {

    @Suppress("LeakingThis") // no problem with leaking this here
    override val checks = CheckLibrary(interactionContext)

    override val actions = ListActions(interactionContext)

    // TODO: remove this constructor and use element fabric method to create an instance
    @Deprecated("Use primary constructor with InteractionContext. This will be removed.")
    constructor(matcher: Matcher<View>) : this(SimpleInteractionContext(matcher))

    /**
     * @param position if null search until first matching by [matcher]
     * if not null search matches by [matcher] at [position] in original item list
     */
    protected inline fun <reified T : PageObjectElement> listElement(
        matcher: Matcher<View>,
        position: Int? = null,
        needScroll: Boolean = true
    ): T =
        T::class.java.getConstructor(InteractionContext::class.java)
            .newInstance(
                RecyclerViewInteractionContext(
                    interactionContext = interactionContext,
                    cellMatcher = Matchers.anyOf(hasDescendant(matcher), matcher),
                    childMatcher = matcher,
                    position = position,
                    needScroll = needScroll
                )
            )

    /**
     * Hide parent method [PageObject.element]
     */
    protected inline fun <reified T : PageObjectElement> element(
        @Suppress("UNUSED_PARAMETER") matcher: Matcher<View>
    ): T = throw RuntimeException("Use listElement(Matcher<View>) instead of element(Matcher<View>)")

    /**
     * Hide parent method [PageObject.element]
     */
    protected inline fun <reified T : PageObjectElement> element(): T =
        throw RuntimeException("Use listElement() instead of element()")

    class ListActions private constructor(
        private val driver: ActionsDriver,
        private val actions: Actions
    ) : Actions by actions {

        val translationY
            get() = ViewGetTranslationYAction().also { driver.perform(it) }.translationY

        val items: Int
            get() = RecyclerViewItemsCountAction().also { driver.perform(it) }.result

        val verticalOffset: Int
            get() = RecyclerViewVerticalOffsetAction().also { driver.perform(it) }.result

        val horizontalOffset: Int
            get() = RecyclerViewHorizontalOffsetAction().also { driver.perform(it) }.result

        constructor(driver: ActionsDriver) : this(driver, ActionsImpl(driver))

        fun scrollToPosition(position: Int) {
            driver.perform(
                com.avito.android.test.espresso.action.recycler.scrollToPosition(
                    position
                )
            )
        }

        fun smoothScrollToPosition(position: Int = 0) {
            driver.perform(SmoothScrollToPositionViewAction(position))
        }

        fun scrollToEnd() = scrollToPosition(items - 1)

        /**
         * @param position if null search until first matching by [matcher]
         * if not null search matches by [matcher] at [position] in original item list
         */
        fun <VH : RecyclerView.ViewHolder> scrollToHolder(
            holder: TypeSafeMatcher<VH>,
            position: Int? = null
        ) {
            driver.perform(
                com.avito.android.test.espresso.action.recycler.scrollToHolder(
                    viewHolderMatcher = holder,
                    position = position
                )
            )
        }

        fun <VH : RecyclerView.ViewHolder> clickOnHolder(
            holder: TypeSafeMatcher<VH>,
            position: Int? = null,
            needScroll: Boolean = true
        ) {
            driver.perform(
                actionOnHolderItem(
                    viewHolderMatcher = holder,
                    viewAction = EspressoActions.click(),
                    position = position,
                    needScroll = needScroll
                )
            )
        }

        fun scrollToChild(
            position: Int = 0,
            targetChildViewId: Int
        ) {
            driver.perform(
                scrollToElementInsideRecyclerViewItem(
                    position = position,
                    childViewId = targetChildViewId
                )
            )
        }

        fun actionOnChild(
            position: Int = 0,
            targetChildViewId: Int,
            childMatcher: Matcher<View>,
            action: ViewAction
        ) {
            scrollToChild(position, targetChildViewId)
            driver.perform(
                ViewActionOnItemAtPosition<RecyclerView.ViewHolder>(
                    position,
                    DescendantViewActions.performDescendantAction(childMatcher, action)
                )
            )
        }

        @Deprecated("Use getItems instead")
        fun countItems() = RecyclerViewItemsCountAction().also { driver.perform(it) }.result

        /**
         * Refreshes recycler view by pressing in it's center and pulling down. Note: it does not perform
         * [scrollToPosition] operation before - developer should do it himself!
         */
        fun pullToRefresh() = actions.swipe(
            object : SwipeDirection {
                override fun toCoordinateProvider(): Pair<CoordinatesProvider, CoordinatesProvider> {
                    return GeneralLocation.CENTER to GeneralLocation.BOTTOM_CENTER
                }
            },
            Swipe.SLOW,
            Press.FINGER
        )

        override fun swipe(
            direction: SwipeDirection,
            speed: Swiper,
            precision: PrecisionDescriber
        ) {
            actions.swipe(
                object : SwipeDirection {
                    override fun toCoordinateProvider():
                        Pair<CoordinatesProvider, CoordinatesProvider> {
                        return when (direction) {
                            TOP_TO_BOTTOM -> GeneralLocation.CENTER to GeneralLocation.BOTTOM_CENTER
                            BOTTOM_TO_TOP -> GeneralLocation.CENTER to GeneralLocation.TOP_CENTER
                            LEFT_TO_RIGHT -> GeneralLocation.CENTER to GeneralLocation.CENTER_RIGHT
                            RIGHT_TO_LEFT -> GeneralLocation.CENTER to GeneralLocation.CENTER_LEFT
                            else -> throw IllegalArgumentException(
                                "Can't do \"swipe\". Argument $direction is not supported"
                            )
                        }
                    }
                },
                Swipe.FAST,
                Press.FINGER
            )
        }

        /**
         * Use only for recyclers with GridLayoutManager
         */
        fun getSpanCount() = RecyclerSpanCountAction().also { driver.perform(it) }.result
    }

    class CheckLibrary(private val driver: ChecksDriver) : Checks by ChecksImpl(driver) {

        override fun withChildCount(countMatcher: Matcher<Int>) {
            driver.check(ViewAssertions.matches(ViewGroupMatcher().hasChildren(countMatcher)))
        }

        override fun withChildCountEquals(count: Int) = withChildCount(`is`(count))

        fun withItemsCount(countMatcher: Matcher<Int>) {
            driver.check(ViewAssertions.matches(RecyclerViewMatcher().itemsInList(countMatcher)))
        }

        fun withItemsCount(count: Int) {
            withItemsCount(equalTo(count))
        }

        fun firstVisiblePosition(positionMatcher: Matcher<Int>) {
            driver.check(
                ViewAssertions.matches(
                    RecyclerViewMatcher().firstVisibleItemPosition(
                        positionMatcher
                    )
                )
            )
        }

        fun hasViewTypeAtPosition(position: Int, viewType: Int) {
            driver.check(
                ViewAssertions.matches(
                    RecyclerViewMatcher().hasViewTypeAtPosition(
                        position,
                        viewType
                    )
                )
            )
        }

        fun doesNotHaveViewTypeAtPosition(position: Int, viewType: Int) {
            driver.check(
                ViewAssertions.matches(
                    RecyclerViewMatcher().doesNotHaveViewTypeAtPosition(
                        position,
                        viewType
                    )
                )
            )
        }

        fun isNotEmpty() = withItemsCount(greaterThan(0))
    }
}
