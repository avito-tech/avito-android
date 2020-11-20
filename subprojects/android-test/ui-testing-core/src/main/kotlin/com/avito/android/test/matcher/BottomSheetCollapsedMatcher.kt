package com.avito.android.test.matcher

import android.view.View
import androidx.test.espresso.matcher.BoundedMatcher
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import org.hamcrest.Description

class BottomSheetCollapsedMatcher : BoundedMatcher<View, View>(View::class.java) {

    override fun describeTo(description: Description) {
        description.appendText("with collapsed bottom sheet")
    }

    override fun matchesSafely(view: View): Boolean =
        BottomSheetBehavior.from(view).state == STATE_COLLAPSED

}
