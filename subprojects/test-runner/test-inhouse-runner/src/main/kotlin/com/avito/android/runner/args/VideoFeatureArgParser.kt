package com.avito.android.runner.args

import com.avito.android.test.report.ArgsProvider
import com.avito.android.test.report.video.VideoFeatureValue

internal class VideoFeatureArgParser : ArgProvider<VideoFeatureValue> {

    override fun parse(args: ArgsProvider): VideoFeatureValue {
        val videoRecordingArgument = args.getArgument("videoRecording")

        return when (args.getArgument("videoRecording")) {
            null, "disabled" -> VideoFeatureValue.Disabled
            "failed" -> VideoFeatureValue.Enabled.OnlyFailed
            "all" -> VideoFeatureValue.Enabled.All
            else -> throw IllegalArgumentException(
                "Failed to resolve video recording resolution from argument: $videoRecordingArgument"
            )
        }
    }
}
