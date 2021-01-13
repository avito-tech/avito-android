package com.avito.android.runner.annotation.resolver

import android.os.Build
import android.util.Log
import com.avito.android.test.annotations.SkipOnSdk

class ImitationModeResolver : TestMetadataResolver {

    override val key: String = BUNDLE_KEY

    override fun resolve(test: TestMethodOrClass): TestMetadataResolver.Resolution {
        val skipOnSdkAnnotation: SkipOnSdk? =
            Annotations.getAnnotationsSubset(test.testClass, test.testMethod, SkipOnSdk::class.java)
                .firstOrNull()
                ?.let { it as SkipOnSdk }

        if (skipOnSdkAnnotation == null) {
            return TestMetadataResolver.Resolution.NothingToChange("SkipOnSdk annotation not found")
        }

        val currentSdk = Build.VERSION.SDK_INT

        if (skipOnSdkAnnotation.sdk.contains(currentSdk)) {
            Log.w(
                "ImitationModeResolver",
                "Test is configured to be skipped on sdk " +
                    "${skipOnSdkAnnotation.sdk.joinToString(", ")} " +
                    "Current sdk is $currentSdk. Real execution will be skipped"
            )

            return TestMetadataResolver.Resolution.ReplaceString("true")
        }

        return TestMetadataResolver.Resolution.NothingToChange("SkipOnSdk doesn't contain $currentSdk sdk")
    }

    companion object {
        const val BUNDLE_KEY = "imitation"
    }
}
