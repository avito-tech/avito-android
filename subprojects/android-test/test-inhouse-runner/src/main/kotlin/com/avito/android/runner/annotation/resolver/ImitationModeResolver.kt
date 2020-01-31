package com.avito.android.runner.annotation.resolver

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import com.avito.android.test.annotations.SkipOnSdk

class ImitationModeResolver : TestMetadataResolver {

    override val key: String = BUNDLE_KEY

    @SuppressLint("LogNotTimber")
    override fun resolve(test: String): TestMetadataResolver.Resolution {
        val methodResolution = MethodStringRepresentation.parseString(test)

        val subset = arrayOf(SkipOnSdk::class.java)

        val skipOnSdkAnnotation: SkipOnSdk? = when (methodResolution) {

            is MethodStringRepresentation.Resolution.ClassOnly ->
                Annotations.getAnnotationsSubset(methodResolution.aClass, null, *subset)
                    .firstOrNull()
                    ?.let { it as SkipOnSdk }

            is MethodStringRepresentation.Resolution.Method ->
                Annotations.getAnnotationsSubset(
                    methodResolution.aClass,
                    methodResolution.method,
                    *subset
                )
                    .firstOrNull()
                    ?.let { it as SkipOnSdk }

            is MethodStringRepresentation.Resolution.ParseError -> null
        }

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
