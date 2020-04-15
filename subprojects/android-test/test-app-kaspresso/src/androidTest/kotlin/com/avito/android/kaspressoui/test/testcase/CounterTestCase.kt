package com.avito.android.kaspressoui.test.testcase

import androidx.test.rule.ActivityTestRule
import com.avito.android.kaspressoui.MainActivity
import com.avito.android.kaspressoui.test.screen.MainScreen
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class CounterTestCase : TestCase() {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java, true, false)

    @Test
    fun testCase() = run {
        step("Launch the app") {
            activityRule.launchActivity(null)

            MainScreen {
                incrementButton.isDisplayed()
                decrementButton.isDisplayed()
                clearButton.isDisplayed()
                valueText.isDisplayed()
            }
        }

        step("Increase value up to five") {
            MainScreen {
                incrementButton {
                    repeat(5) {
                        click()
                    }
                }

                assertValue(5)
            }
        }

        step("Decrease value down to three") {
            MainScreen {
                decrementButton {
                    repeat(2) {
                        click()
                    }
                }

                assertValue(3)
            }
        }

        step("Clear the value") {
            MainScreen() {
                clearButton.click()
                assertValue(0)
            }
        }
    }
}
