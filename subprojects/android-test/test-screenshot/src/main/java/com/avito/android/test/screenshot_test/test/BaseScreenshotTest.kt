package com.avito.android.test.screenshot_test.test

import android.Manifest
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingPolicies
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import com.avito.android.runner.RemoteStorageProvider
import com.avito.android.test.Device
import com.avito.android.test.report.ReportProvider
import com.avito.android.test.screenshot_test.internal.BitmapSaver
import com.avito.android.test.screenshot_test.internal.ScreenshotComparisonReporter
import com.avito.android.test.screenshot_test.internal.ScreenshotDirectory
import com.avito.android.test.screenshot_test.internal.ViewScreenshotMaker
import com.avito.android.test.screenshot_test.internal.getBitmapFromAsset
import com.avito.android.test.screenshot_test.internal.getBitmapFromDevice
import com.avito.filestorage.RemoteStorage
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Assert
import org.junit.Before
import java.util.concurrent.TimeUnit

abstract class BaseScreenshotTest<T : View>(
    private val styleAttrs: List<Int> = listOf(),
    private val themes: List<TestTheme> = listOf()
) {

    abstract val activity: IdlieableActivity

    @Suppress("PlatformExtensionReceiverOfInline")
    private val recordMode: Boolean
        get() = InstrumentationRegistry.getArguments().getString("recordScreenshots", "false").toBoolean()

    private val screenshotDir: ScreenshotDirectory by lazy {
        ScreenshotDirectory.create(activity, "screenshots")
    }

    private val referenceScreenshotDir: ScreenshotDirectory by lazy {
        ScreenshotDirectory.create(activity, "reference_screenshots")
    }

    private val remoteStorage: RemoteStorage by lazy {
        (InstrumentationRegistry.getInstrumentation() as RemoteStorageProvider).remoteStorage
    }

    private val screenshotComparisonReporter: ScreenshotComparisonReporter by lazy {
        ScreenshotComparisonReporter(
            remoteStorage = (InstrumentationRegistry.getInstrumentation() as RemoteStorageProvider).remoteStorage,
            report = (InstrumentationRegistry.getInstrumentation() as ReportProvider).report,
            context = InstrumentationRegistry.getInstrumentation().targetContext
        )
    }

    abstract fun createView(
        context: Context,
        styleAttr: Int
    ): T

    abstract fun createViewStates(): HashMap<String, (view: T) -> Unit>

    private val screenshotNames: ArrayList<String> = arrayListOf()

    @Before
    fun onBefore() {
        IdlingPolicies.setMasterPolicyTimeout(1, TimeUnit.MINUTES)
        IdlingPolicies.setIdlingResourceTimeout(1, TimeUnit.MINUTES)
        IdlingRegistry.getInstance().register(activity.countingIdlingResource)
    }

    @After
    fun onAfter() {
        IdlingRegistry.getInstance().unregister(activity.countingIdlingResource)
    }

    fun compareScreenshotsTest() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Device.grantPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            createScreenshots("Light")
            activity.runOnUiThread {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            createScreenshots("Dark")
            if (!recordMode) {
                compareScreens()
            }
        } else {
            throw IllegalStateException("BaseScreenshotTest supports SDK greater or equal to ${Build.VERSION_CODES.O}. Current is ${Build.VERSION.SDK_INT}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createScreenshots(mode: String) {
        themes.forEach { theme ->
            styleAttrs.forEach { attr ->
                createViewStates().forEach { (stateName, applyActionToView) ->
                    initEnvironment(attr, theme, stateName, mode) { context, screenShotName ->
                        val view = createView(context, attr)
                        applyActionToView(view)
                        addView(view)
                        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
                        Espresso.onView(
                            Matchers.allOf(
                                ViewMatchers.isDisplayed(),
                                ViewMatchers.isAssignableFrom(view::class.java)
                            )
                        ).check { displayedView, noViewFoundException ->
                            noViewFoundException?.also { throw it }
                            Assert.assertEquals("Added view is displayed", displayedView, view)
                        }
                        screenshotNames.add(screenShotName)
                        activity.countingIdlingResource.increment()

                        ViewScreenshotMaker(
                            activity,
                            screenshotDir.getScreenshot(screenShotName)
                        ).makeScreenshot(view)

                        activity.countingIdlingResource.decrement()
                        removeView(view)
                    }
                }
            }
        }
    }

    private fun initEnvironment(
        styleAttr: Int,
        theme: TestTheme,
        stateName: String,
        mode: String,
        action: (context: Context, filename: String) -> Unit
    ) {
        val context = ContextThemeWrapper(activity, theme.theme)
        val themeName = theme.name.replace(" ", "_")
        val styleName =
            if (styleAttr != 0) context.resources.getResourceEntryName(styleAttr) else ""
        val fileName = "${themeName}_${mode}_${styleName}_$stateName"
        action(context, fileName)
    }

    private fun compareScreens() {
        //the only purpose of this line is to freeze work if countingIdlingResource requires
        Espresso.onIdle()
        val context = InstrumentationRegistry.getInstrumentation().context
        screenshotNames.forEach { screenshotName ->
            val generatedScreenshot = screenshotDir.getScreenshot(screenshotName)
            val relativeScreenshotFilePath = "screenshots/${generatedScreenshot.emulatorSpecificPath}"
            val generatedBitmap = getBitmapFromDevice(generatedScreenshot.path)
            val referenceBitmap = context.getBitmapFromAsset(relativeScreenshotFilePath)
            val referenceScreenshot = referenceScreenshotDir.getScreenshot(screenshotName)

            if (generatedBitmap.height != referenceBitmap.height || generatedBitmap.width != referenceBitmap.width) {
                BitmapSaver().save(referenceBitmap, referenceScreenshot)
                screenshotComparisonReporter.reportScreenshotComparison(
                    generated = generatedScreenshot,
                    reference = referenceScreenshot
                )
                throw AssertionError(
                    """Bitmaps for $relativeScreenshotFilePath have different sizes (width,height):
                Generated bitmap: (${generatedBitmap.width},${generatedBitmap.height})
                Reference bitmap: (${referenceBitmap.width},${referenceBitmap.height})
            """.trimIndent()
                )
            }

            if (!referenceBitmap.sameAs(generatedBitmap)) {
                BitmapSaver().save(referenceBitmap, referenceScreenshot)
                screenshotComparisonReporter.reportScreenshotComparison(
                    generated = generatedScreenshot,
                    reference = referenceScreenshot
                )
                throw AssertionError("Generated bitmap: $relativeScreenshotFilePath doesn't equal to reference bitmap")
            }
        }
    }

    private fun getRootView(): LinearLayout {
        return activity.findViewById<ViewGroup>(android.R.id.content)!!
            .getChildAt(0) as LinearLayout
    }

    private fun addView(view: T) {
        activity.runOnUiThread {
            val rootView = getRootView()
            rootView.addView(view)
        }
    }

    private fun removeView(view: T) {
        activity.runOnUiThread {
            val rootView = getRootView()
            rootView.removeView(view)
        }
    }
}
