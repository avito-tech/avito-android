package com.avito.android.test.report.video

import android.os.Build
import com.avito.report.model.Incident

/**
 * Единое место отвечающее за принятие решения о записи и заливке видео теста.
 * Логика разделена на 2 метода т.к валидным является кейс, когда мы записываем видео, но потом
 * принимаем решение о его незаливке, например, если тест прошел.
 */
interface VideoFeature {
    fun videoRecordingEnabled(shouldRecord: Boolean): Boolean
    fun videoUploadingEnabled(shouldRecord: Boolean, incident: Incident?): Boolean
}

class VideoFeatureImplementation(
    private val videoFeatureValue: VideoFeatureValue,
    private val canRecord: Boolean = Build.VERSION.SDK_INT >= 23 && videoFeatureValue is VideoFeatureValue.Enabled
) : VideoFeature {

    override fun videoRecordingEnabled(shouldRecord: Boolean): Boolean = shouldRecord && canRecord

    override fun videoUploadingEnabled(shouldRecord: Boolean, incident: Incident?): Boolean =
        videoRecordingEnabled(shouldRecord) &&
            (videoFeatureValue is VideoFeatureValue.Enabled.All ||
                videoFeatureValue is VideoFeatureValue.Enabled.OnlyFailed && incident != null)
}

sealed class VideoFeatureValue {
    object Disabled : VideoFeatureValue()

    sealed class Enabled : VideoFeatureValue() {
        object OnlyFailed : Enabled()
        object All : Enabled()
    }
}
