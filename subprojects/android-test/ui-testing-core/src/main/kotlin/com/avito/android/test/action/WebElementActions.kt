package com.avito.android.test.action

import androidx.test.espresso.action.PrecisionDescriber
import androidx.test.espresso.action.SwipeDirection
import androidx.test.espresso.action.Swiper
import androidx.test.espresso.web.sugar.Web
import androidx.test.espresso.web.webdriver.DriverAtoms

class WebElementActions(private val interaction: Web.WebInteraction<Void>) : Actions {

    override fun click() {
        interaction.perform(DriverAtoms.webScrollIntoView())
        interaction.perform(DriverAtoms.webClick())
    }

    @Deprecated("Not supported on the Web View", replaceWith = ReplaceWith("click()"))
    override fun longClick() {
        throw UnsupportedOperationException("Long click is not supported on the Web View")
    }

    override fun scrollTo() {
        interaction.perform(DriverAtoms.webScrollIntoView())
    }

    @Deprecated("Not supported on the Web View")
    override fun swipe(direction: SwipeDirection, speed: Swiper, precision: PrecisionDescriber) {
        throw UnsupportedOperationException("Swipe is not supported on the Web View")
    }

    // todo is there an async problem?
    override fun read(allowBlank: Boolean): String {
        return interaction.perform(DriverAtoms.getText()).get()
    }

    fun write(text: String) {
        interaction.perform(DriverAtoms.webKeys(text))
    }
}
