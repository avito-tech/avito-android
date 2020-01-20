package com.avito.android.test.page_object

import androidx.appcompat.widget.DialogTitle
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId

class Alert : PageObject() {
    val messageElement: ViewElement = element(withId(android.R.id.message))
    val title: ViewElement = element(ViewMatchers.isAssignableFrom(DialogTitle::class.java))
    val okButton: ViewElement = element(withId(android.R.id.button1))
    val negativeButton: ViewElement = element(withId(android.R.id.button2))
    val neutralButton: ViewElement = element(withId(android.R.id.button3))
}
