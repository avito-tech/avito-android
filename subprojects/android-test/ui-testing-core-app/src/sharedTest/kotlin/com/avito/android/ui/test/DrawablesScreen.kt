package com.avito.android.ui.test

import androidx.test.espresso.matcher.ViewMatchers.withId
import com.avito.android.test.page_object.ImageViewElement
import com.avito.android.test.page_object.SimpleScreen
import com.avito.android.test.page_object.ViewElement
import com.avito.android.ui.R

class DrawablesScreen : SimpleScreen() {

    override val rootId: Int = R.id.drawables

    val viewWithBackgroundColor: ViewElement = element(withId(R.id.background_view_color))

    val viewWithBackgroundImage: ViewElement = element(withId(R.id.background_view_image))

    val viewWithBackgroundImageWithTint: ViewElement = element(withId(R.id.background_view_image_with_tint))

    val textViewWithDrawable: ViewElement = element(withId(R.id.text_view))

    val imageView: ImageViewElement = element(withId(R.id.image_view))

    val imageViewWithTint: ImageViewElement = element(withId(R.id.image_view_with_tint))
}
