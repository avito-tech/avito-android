package com.avito.android.test.matcher

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.widget.ImageView
import androidx.test.espresso.matcher.BoundedMatcher
import junit.framework.AssertionFailedError
import org.hamcrest.Description
import kotlin.math.absoluteValue

class BitmapMatcher(private val bitmap: Bitmap) :
    BoundedMatcher<View, ImageView>(ImageView::class.java) {

    override fun describeTo(description: Description) {
        description.appendText("with bitmap: ")
    }

    override fun matchesSafely(layout: ImageView): Boolean {
        val currentBitmap = (layout.drawable as? BitmapDrawable)?.bitmap
        val shouldBe = bitmap.getTopLeftPixels()
        val actual = currentBitmap?.getTopLeftPixels()
            ?: throw AssertionFailedError("This Drawable is not Bitmap or not exist")

        val i = shouldBe.iterator()
        val j = actual.iterator()
        while (i.hasNext() && j.hasNext()) {
            if (!hasSameColor(i.next(), j.next())) {
                throw AssertionFailedError("Bitmaps has different colors!")
            }
        }
        return true
    }

    @Suppress("CustomColorsKotlin")
    private fun hasSameColor(pixel1: Int, pixel2: Int): Boolean {
        val r = (Color.red(pixel1) - Color.red(pixel2)).absoluteValue
        val g = (Color.green(pixel1) - Color.green(pixel2)).absoluteValue
        val b = (Color.blue(pixel1) - Color.blue(pixel2)).absoluteValue
        // As we do some on-device-compression there is confidence interval
        return r < CONFIDENCE_INTERVAL && g < CONFIDENCE_INTERVAL && b < CONFIDENCE_INTERVAL
    }

    private fun Bitmap.getTopLeftPixels(): List<Int> {
        val result = mutableListOf<Int>()
        for (i in 5..30 step 5) {
            for (j in 5..30 step 5) {
                result.add(getPixel(i, j))
            }
        }
        return result
    }
}

private const val CONFIDENCE_INTERVAL = 10
