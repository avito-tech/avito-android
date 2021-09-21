package com.avito.android.ui.test

import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.avito.android.test.page_object.SimpleScreen
import com.avito.android.test.page_object.ViewElement
import com.avito.android.ui.R

class ButtonsScreen : SimpleScreen() {

    override val rootId: Int = R.id.activity_buttons_root

    val enabledButton: ViewElement = element(withId(R.id.button_enabled))

    val enabledButtonClickIndicatorView: ViewElement = element(
        withId(R.id.button_enabled_clicked_text_view)
    )

    val enabledButtonLongClickIndicatorView: ViewElement = element(
        withId(R.id.button_enabled_long_clicked_text_view)
    )

    val disabledButton: ViewElement = element(withId(R.id.button_disabled))

    val nonClickableButton: ViewElement = element(withId(R.id.button_non_clickable))

    val clickableContainerInnerButton: ViewElement = element(withText("Non clickable inside clickable"))

    val clickableContainerIndicator: ViewElement = element(withId(R.id.clickable_container_indicator))

    val nonLongClickableButton: ViewElement = element(withId(R.id.button_non_long_clickable))

    val nonLongClickableButtonIndicator: ViewElement = element(
        withId(R.id.button_non_long_clickable_clicked_text_view)
    )

    val longClickableContainerInnerButton: ViewElement = element(withText("Non long-clickable inside long-clickable"))

    val longClickableContainerIndicator: ViewElement = element(withId(R.id.long_clickable_container_indicator))

    val overlappedByClickable = element<ViewElement>(withId(R.id.overlapped_by_clickable_button))

    val overlappedByNonClickable = element<ViewElement>(withId(R.id.overlapped_by_non_clickable_button))

    val hiddenButton: ViewElement = element(withId(R.id.hidden_button))

    val hiddenButtonIndicator: ViewElement = element(withId(R.id.hidden_button_click_indicator))

    val animatedView: ViewElement = element(withId(R.id.animated_view))

    val animatedViewIndicator: ViewElement = element(withId(R.id.animated_view_click_indicator))
}
