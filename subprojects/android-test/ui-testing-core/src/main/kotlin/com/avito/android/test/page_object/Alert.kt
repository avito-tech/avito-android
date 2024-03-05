package com.avito.android.test.page_object

import androidx.appcompat.widget.AlertDialogLayout
import androidx.appcompat.widget.DialogTitle
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.withId

public open class Alert : DialogScreen(
    matcher = isAssignableFrom(AlertDialogLayout::class.java)
) {
    public val messageElement: ViewElement = element(withId(android.R.id.message))
    public val title: ViewElement = element(isAssignableFrom(DialogTitle::class.java))
    public val okButton: ViewElement = element(withId(android.R.id.button1))
    public val negativeButton: ViewElement = element(withId(android.R.id.button2))
    public val neutralButton: ViewElement = element(withId(android.R.id.button3))
}
