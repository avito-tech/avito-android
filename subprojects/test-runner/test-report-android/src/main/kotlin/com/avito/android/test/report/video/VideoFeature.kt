package com.avito.android.test.report.video

import com.avito.report.model.Incident

/**
 * Единое место отвечающее за принятие решения о записи и заливке видео теста.
 * Логика разделена на 2 метода т.к валидным является кейс, когда мы записываем видео, но потом
 * принимаем решение о его незаливке, например, если тест прошел.
 */
public interface VideoFeature {
    public fun videoRecordingEnabled(shouldRecord: Boolean): Boolean
    public fun videoUploadingEnabled(shouldRecord: Boolean, incident: Incident?): Boolean
}

internal class VideoFeatureImplementation(
    private val videoFeatureValue: VideoFeatureValue,
    private val canRecord: Boolean = videoFeatureValue is VideoFeatureValue.Enabled
) : VideoFeature {

    override fun videoRecordingEnabled(shouldRecord: Boolean): Boolean = shouldRecord && canRecord

    override fun videoUploadingEnabled(shouldRecord: Boolean, incident: Incident?): Boolean =
        videoRecordingEnabled(shouldRecord) &&
            (videoFeatureValue is VideoFeatureValue.Enabled.All ||
                videoFeatureValue is VideoFeatureValue.Enabled.OnlyFailed && incident != null)
}

public sealed class VideoFeatureValue {
    public data object Disabled : VideoFeatureValue()

    public sealed class Enabled : VideoFeatureValue() {
        public data object OnlyFailed : Enabled()
        public data object All : Enabled()
    }
}
