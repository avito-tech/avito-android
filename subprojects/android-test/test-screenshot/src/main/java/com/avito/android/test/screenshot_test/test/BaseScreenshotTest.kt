package com.avito.android.test.screenshot_test.test

import android.Manifest
import android.content.Context
import android.os.Build
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
import com.avito.android.test.Device
import com.avito.android.test.report.ReportProvider
import com.avito.android.test.screenshot_test.internal.DeviceDirectoryName
import com.avito.android.test.screenshot_test.internal.getBitmapFromAsset
import com.avito.android.test.screenshot_test.internal.getBitmapFromDevice
import com.avito.android.test.screenshot_test.internal.getFileFromAsset
import com.avito.android.test.screenshot_test.internal.getFileFromDevice
import com.avito.android.test.screenshot_test.internal.saveScreenshot
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Assert
import org.junit.Before
import java.io.InputStream
import java.util.concurrent.TimeUnit

abstract class BaseScreenshotTest<T : View>(
    private val styleAttrs: List<Int> = listOf(),
    private val themes: List<TestTheme> = listOf()
) {

    abstract val activity: IdlieableActivity

    abstract fun createView(
        context: Context,
        styleAttr: Int
    ): T

    abstract fun createViewStates(): HashMap<String, (view: T) -> Unit>

    private val report = (InstrumentationRegistry.getInstrumentation() as ReportProvider).report

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

    open fun onViewAdded(view: T) {
        //override if you need callback after view is added
    }

    open fun onViewRemoved(view: T) {
        //override if you need callback after view is removed
    }

    fun compareScreenshotsTest() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Device.grantPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            createScreenshots("Light")
            runOnUiThread {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            createScreenshots("Dark")
            compareScreens()
        } else {
            throw IllegalStateException("BaseScreenshotTest supports SDK greater or equal to ${Build.VERSION_CODES.O}. Current is ${Build.VERSION.SDK_INT}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createScreenshots(mode: String) {
        themes.forEach { theme ->
            styleAttrs.forEach { attr ->
                createViewStates().forEach { (stateName, applyActionToView) ->
                    initEnvironment(attr, theme, stateName, mode) { context, screenShotFileName ->
                        val view = createView(context, attr)
                        applyActionToView(view)
                        addView(view)
                        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
                        Espresso.onView(Matchers.allOf(
                            ViewMatchers.isDisplayed(),
                            ViewMatchers.isAssignableFrom(view::class.java)
                        )).check { displayedView, noViewFoundException ->
                            noViewFoundException?.also { throw it }
                            Assert.assertEquals("Added view is displayed", displayedView, view)
                        }
                        screenshotNames.add(screenShotFileName)
                        activity.countingIdlingResource.increment()
                        view.saveScreenshot(activity, screenShotFileName)
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
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val context = InstrumentationRegistry.getInstrumentation().context
        val directoryName = DeviceDirectoryName.create(targetContext).name
        screenshotNames.forEach { name ->
            val screenshotFilePath = "$directoryName/$name"
            val referenceBitmap = getBitmapFromAsset(context, screenshotFilePath)
            val generatedBitmap = getBitmapFromDevice(targetContext, screenshotFilePath)

            if (generatedBitmap.height != referenceBitmap.height || generatedBitmap.width != referenceBitmap.width) {
                report(screenshotFilePath)
                throw AssertionError(
                    """Bitmaps for $screenshotFilePath have different sizes (width,height):
                Generated bitmap: (${generatedBitmap.width},${generatedBitmap.height})
                Reference bitmap: (${referenceBitmap.width},${referenceBitmap.height})
            """.trimIndent()
                )
            }

            if (!referenceBitmap.sameAs(generatedBitmap)) {
                report(screenshotFilePath)
                throw AssertionError("Generated bitmap: $screenshotFilePath doesn't equal to reference bitmap")
            }
        }
    }

    private fun getRootView(): LinearLayout {
        return activity.findViewById<ViewGroup>(android.R.id.content)!!
            .getChildAt(0) as LinearLayout
    }

    private fun runOnUiThread(action: (rootView: ViewGroup) -> Unit) {
        val rootView = getRootView()
        activity.runOnUiThread {
            action(rootView)
        }
    }

    private fun addView(view: T) {
        runOnUiThread { rootView ->
            rootView.addView(view)
            onViewAdded(view)
        }
    }

    private fun removeView(view: T) {
        runOnUiThread { rootView ->
            onViewRemoved(view)
            rootView.removeView(view)
        }
    }

    private fun report(filePath: String) {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val context = InstrumentationRegistry.getInstrumentation().context
        val referenceUrl = report.addImageSynchronously(getFileFromAsset(context, filePath))
        val generatedUrl = report.addImageSynchronously(getFileFromDevice(targetContext, filePath))
        val htmlReport = getReportAsString(referenceUrl, generatedUrl)
        report.addHtml(
            label = "Press me to see report",
            content = htmlReport,
            wrapHtml = false
        )
    }

    private fun getReportAsString(referenceUrl: String?, generatedUrl: String?): String {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val inputStream: InputStream = targetContext.assets.open("screenshot_test_report.html")
        val size: Int = inputStream.available()
        val buffer = ByteArray(size)
        inputStream.read(buffer)
        inputStream.close()

        var result = String(buffer)
        result = result.replace("%referenceImage%", referenceUrl ?: "")
        result = result.replace("%generatedImage%", generatedUrl ?: "")
        return result

    }
}
