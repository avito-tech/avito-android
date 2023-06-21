package com.avito.android.test.matcher

import android.view.View
import androidx.test.espresso.matcher.BoundedMatcher
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import org.hamcrest.Description

class BottomSheetHiddenMatcher : BoundedMatcher<View, View>(View::class.java) {

    override fun describeTo(description: Description) {
        description.appendText("with hidden bottom sheet")
    }

    override fun matchesSafely(view: View): Boolean =
        BottomSheetBehavior.from(view).state == STATE_HIDDEN
}
