package com.avito.android.test.screenshot_test.test

import android.Manifest
import android.content.Context
import android.os.Build
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatDelegate
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingPolicies
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import com.avito.android.test.Device
import com.avito.android.test.report.ReportProvider
import com.avito.android.test.screenshot_test.internal.*
import org.junit.After
import org.junit.Assert
import org.junit.Before
import java.io.InputStream
import java.util.concurrent.TimeUnit

abstract class BaseScreenshotTest<T : View> {

    open val styleAttrs = listOf<Int>()

    open val themes = listOf<TestTheme>()

    open var view: T? = null

    abstract fun createView(
        context: Context,
        styleAttr: Int,
        applyActionToView: (view: T) -> Unit
    ): T

    abstract fun createViewStates(): HashMap<String, (view: T) -> Unit>

    abstract fun getActivity(): IdlieableActivity

    var statesMap = mutableMapOf<String, (view: T) -> Unit>()

    val report = (InstrumentationRegistry.getInstrumentation() as ReportProvider).report

    private var screenshotNames: ArrayList<String> = arrayListOf()

    @Before
    fun onBefore() {
        IdlingPolicies.setMasterPolicyTimeout(1, TimeUnit.MINUTES)
        IdlingPolicies.setIdlingResourceTimeout(1, TimeUnit.MINUTES)
        IdlingRegistry.getInstance().register(getActivity().countingIdlingResource)
    }

    @After
    fun onAfter() {
        IdlingRegistry.getInstance().unregister(getActivity().countingIdlingResource)
    }

    open fun onViewAdded(view: T) {
        //override if you need callback after view is added
    }

    open fun onViewRemoved(view: T) {
        //override if you need callback after view is removed
    }

    fun getRootView(): LinearLayout {
        return getActivity().findViewById<ViewGroup>(android.R.id.content)!!
            .getChildAt(0) as LinearLayout
    }

    fun runOnUiThread(action: (rootView: ViewGroup) -> Unit) {
        val activity = getActivity()
        val rootView = getRootView()
        activity.runOnUiThread {
            action(rootView)
        }
    }

    fun compareScreenshotsTest() {
        if (Build.VERSION.SDK_INT < 24) {
            throw Exception("This SDK version is not supported")
        }
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
    }

    fun createScreenshots(mode: String) {
        themes.forEach { theme ->
            styleAttrs.forEach { attr ->
                createViewStates().forEach { (name, applyActionToView) ->
                    initEnvironment(attr, theme, name, mode) { context, fileName ->
                        view = createView(context, attr, applyActionToView)
                        applyActionToView(view!!)
                        addView(view!!)
                        onViewAdded(view!!)
                        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
                        Espresso.onView(ViewMatchers.isDisplayed())
                        screenshotNames.add(fileName)
                        view!!.saveScreenshot(getActivity(), fileName)
                        onViewRemoved(view!!)
                        removeView(view)
                    }
                }
            }
        }
    }

    private fun addView(view: T) {
        runOnUiThread { rootView ->
            rootView.addView(view)
        }
    }

    private fun removeView(view: View?) {
        if (view == null) return
        runOnUiThread { rootView ->
            rootView.removeView(view)
        }
    }

    private fun compareScreens() {
        //the only purpose of this line is to freeze work if countingIdlingResource requires
        Espresso.onIdle()
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val context = InstrumentationRegistry.getInstrumentation().context
        val directoryName = DeviceDirectoryName.create(targetContext).name
        screenshotNames.forEach { name ->
            val filePath = "$directoryName/$name"
            val referenceBitmap = getBitmapFromAsset(context, filePath)
            val currentBitmap = getBitmapFromDevice(targetContext, filePath)
            if (referenceBitmap == null) {
                report(filePath)
                throw Exception("Reference bitmap for $filePath is null")
            }
            if (currentBitmap == null) {
                report(filePath)
                throw Exception("Generated bitmap for $filePath is null")
            }
            if (currentBitmap.height != referenceBitmap.height || currentBitmap.width != referenceBitmap.width) {
                report(filePath)
                throw Exception(
                    "Bitmaps for $filePath have different sizes (width,height): " +
                            "GeneratedBitmap: (${currentBitmap.width},${currentBitmap.height}) " +
                            "ReferenceBitmap : (${referenceBitmap.width},${referenceBitmap.height})"
                )
            }
            val result = referenceBitmap.sameAs(currentBitmap)
            if (!result) {
                report(filePath)
            }
            Assert.assertTrue("$filePath does not equals reference", result)
        }
        IdlingRegistry.getInstance().unregister(getActivity().countingIdlingResource)
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
            wrapHtml = false,
            stepName = "Screenshot Сomparison"
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

    private fun initEnvironment(
        styleAttr: Int,
        theme: TestTheme,
        name: String,
        mode: String,
        action: (context: Context, filename: String) -> Unit
    ) {
        val activity = getActivity()
        val context = ContextThemeWrapper(activity, theme.theme)
        val themeName = theme.name.replace(" ", "_")
        val styleName =
            if (styleAttr != 0) context.resources.getResourceEntryName(styleAttr) else ""
        val fileName = "${themeName}_${mode}_${styleName}_$name"
        action(context, fileName)
    }
}