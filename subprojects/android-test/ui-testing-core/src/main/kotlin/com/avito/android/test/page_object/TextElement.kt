package com.avito.android.test.page_object

import android.text.SpannableString
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.CoordinatesProvider
import androidx.test.espresso.util.HumanReadables
import com.avito.android.test.InteractionContext
import com.avito.android.test.action.Actions
import com.avito.android.test.action.ActionsDriver
import com.avito.android.test.action.ActionsImpl
import com.avito.android.test.espresso.EspressoActions
import org.hamcrest.Matcher
import org.hamcrest.Matchers

class TextElement(interactionContext: InteractionContext) : ViewElement(interactionContext) {

    override val actions: TextElementActions = TextElementActionsImpl(interactionContext)

    fun clickOnText(textToClick: String) = actions.clickOnText(textToClick)
    fun clickOnLink() = actions.clickOnLink()
}

interface TextElementActions : Actions {
    fun clickOnText(textToClick: String)
    fun clickOnLink()
}

class TextElementActionsImpl(private val driver: ActionsDriver) : TextElementActions,
    Actions by ActionsImpl(driver) {

    override fun clickOnText(textToClick: String) {
        driver.perform(ClickOnTextAction(textToClick))
    }

    override fun clickOnLink() {
        driver.perform(ClickOnSpannableAction())
    }
}

class ClickOnTextAction(private val textToClick: String) : ViewAction {

    override fun getConstraints(): Matcher<View> = Matchers.instanceOf(TextView::class.java)

    override fun getDescription() = "clicking on a specified text in textView"

    override fun perform(uiController: UiController, view: View) {
        val textView = view as TextView
        val text = textView.text

        val count = text.split(textToClick).size - 1
        if (count != 1) {
            throw throw PerformException.Builder().withActionDescription(this.toString())
                .withViewDescription(HumanReadables.describe(view))
                .withCause(
                    IllegalStateException(
                        "Zero or more than 1 matches for given text in TextView. " +
                            "Please specify text to click more accurately"
                    )
                )
                .build()
        }

        val start = text.indexOf(textToClick)
        val end = start + textToClick.length - 1
        val coordinatesProvider = getTextCenterProvider(textView, start, end)

        EspressoActions.click(coordinatesProvider = coordinatesProvider).perform(uiController, textView)
    }
}

class ClickOnSpannableAction : ViewAction {

    override fun getConstraints(): Matcher<View> = Matchers.instanceOf(TextView::class.java)

    override fun getDescription() = "clicking on a clickable span in textView"

    override fun perform(uiController: UiController, view: View) {
        val textView = view as TextView
        val text = textView.text as SpannableString
        val spans = text.getSpans(0, text.length, ClickableSpan::class.java)

        val count = spans.size
        if (count != 1) {
            throw throw PerformException.Builder().withActionDescription(this.toString())
                .withViewDescription(HumanReadables.describe(view))
                .withCause(IllegalStateException("TextView doesn't contain clickableSpans"))
                .build()
        }

        val textToClick = spans.first()
        val start = text.getSpanStart(textToClick)
        val end = text.getSpanEnd(textToClick)

        val coordinatesProvider = getTextCenterProvider(textView, start, end)

        EspressoActions.click(coordinatesProvider = coordinatesProvider).perform(uiController, textView)
    }
}

private fun getTextCenterProvider(textView: TextView, start: Int, end: Int): CoordinatesProvider {
    val layout = textView.layout
    val startOffset = layout.getPrimaryHorizontal(start)
    val endOffset = layout.getPrimaryHorizontal(end)

    val startLine = layout.getLineForOffset(start)
    val endLine = layout.getLineForOffset(end)
    val lines = endLine - startLine + 1

    val textWidth = if (lines == 1) {
        maxOf(endOffset - startOffset, 0f)
    } else {
        layout.getLineRight(startLine) - startOffset
    }

    val lineHeight = layout.getLineBottom(startLine) - layout.getLineTop(startLine)

    val viewPosition = IntArray(2)
    textView.getLocationOnScreen(viewPosition)

    val coordinates = FloatArray(2)
    coordinates[0] = viewPosition[0] + startOffset + textWidth / 2
    coordinates[1] = viewPosition[1] + layout.getLineTop(startLine) + lineHeight / 2f
    return CoordinatesProvider { coordinates }
}
