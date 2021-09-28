package com.avito.android.ui.test

import androidx.test.espresso.matcher.ViewMatchers.withId
import com.avito.android.test.page_object.ImageViewElement
import com.avito.android.test.page_object.SimpleScreen
import com.avito.android.ui.R

class BitmapScreen : SimpleScreen() {

    override val rootId: Int = R.id.bitmaps

    val imageViewBitmap: ImageViewElement = element(withId(R.id.image_view_bitmap))

    val imageViewVector: ImageViewElement = element(withId(R.id.image_view_vector))
}
