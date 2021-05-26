package com.avito.android.ui.test

import com.avito.android.test.UITestConfig.ClickType.EspressoClick
import com.avito.android.test.app.core.screenRule
import com.avito.android.test.util.ChangeClickType
import com.avito.android.test.util.ClicksTypeRule
import com.avito.android.ui.TextElementActivity
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test

class TextElementsTest {

    @get:Rule
    val rule = screenRule<TextElementActivity>()

    @get:Rule
    val clicksRule = ClicksTypeRule(EspressoClick(EspressoClick.ClickRollbackPolicy.DoNothing))

    @Test
    @ChangeClickType
    fun clicksOnLink_espressoClicks() {
        rule.launchActivity(null)
        Screen.textsElements.textView.clickOnLink()
        rule.onActivity {
            assertThat(count).isEqualTo(1)
        }
    }

    @Test
    @ChangeClickType
    fun clicksOnText_espressoClicks() {
        rule.launchActivity(null)
        Screen.textsElements.textView.clickOnText("link")
        rule.onActivity {
            assertThat(count).isEqualTo(1)
        }
    }

    @Test
    @ChangeClickType
    fun clicksOnSeveralLinks_espressoClicks() {
        rule.launchActivity(null)
        Screen.textsElements.textViewLong.clickOnText("link 1")
        Screen.textsElements.textViewLong.clickOnText("link 2")
        Screen.textsElements.textViewLong.clickOnText("long link which can have\nmultiple lines")
        rule.onActivity {
            assertThat(count).isEqualTo(3)
        }
    }

    @Test
    fun clicksOnLink() {
        rule.launchActivity(null)
        Screen.textsElements.textView.clickOnLink()
        rule.onActivity {
            assertThat(count).isEqualTo(1)
        }
    }

    @Test
    fun clicksOnText() {
        rule.launchActivity(null)
        Screen.textsElements.textView.clickOnText("link")
        rule.onActivity {
            assertThat(count).isEqualTo(1)
        }
    }

    @Test
    fun clicksOnSeveralLinks() {
        rule.launchActivity(null)
        Screen.textsElements.textViewLong.clickOnText("link 1")
        Screen.textsElements.textViewLong.clickOnText("link 2")
        Screen.textsElements.textViewLong.clickOnText("long link which can have\nmultiple lines")
        rule.onActivity {
            assertThat(count).isEqualTo(3)
        }
    }
}
