package com.avito.android.test.matcher

import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.google.android.material.textfield.TextInputLayout
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

class HintMatcher(val hint: String) : TypeSafeMatcher<View>() {

    override fun describeTo(description: Description) {
        description.appendText("with hint: $hint")
    }

    public override fun matchesSafely(view: View) =
        when (view) {
            is EditText -> {
                val textInputLayout = view.findParentTextInputLayoutRecursively()
                view.hint == hint || textInputLayout?.hint == hint
            }
            is TextView -> view.hint == hint
            else -> false
        }
}

// not a bad name for a private function
@Suppress("FunctionMaxLength")
private fun View.findParentTextInputLayoutRecursively(): TextInputLayout? {
    val view = this.parent as? View
    val textInput = view ?: return null
    return textInput as? TextInputLayout ?: textInput.findParentTextInputLayoutRecursively()
}
