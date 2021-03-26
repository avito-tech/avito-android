package com.avito.android.runner.annotation.resolver

import android.os.Build
import com.avito.android.elastic.ElasticConfig
import com.avito.android.log.AndroidLoggerFactory
import com.avito.android.sentry.SentryConfig
import com.avito.android.test.annotations.SkipOnSdk
import com.avito.logger.create

/**
 * Used in Avito
 */
class ImitationModeResolver : TestMetadataResolver {

    private val logger = AndroidLoggerFactory(
        elasticConfig = ElasticConfig.Disabled,
        sentryConfig = SentryConfig.Disabled,
        testName = null
    ).create<ImitationModeResolver>()

    override val key: String = BUNDLE_KEY

    override fun resolve(test: TestMethodOrClass): TestMetadataResolver.Resolution {
        val skipOnSdkAnnotation: SkipOnSdk? =
            Annotations.getAnnotationsSubset(test.testClass, test.testMethod, SkipOnSdk::class.java)
                .firstOrNull()
                ?.let { it as SkipOnSdk }

        @Suppress("FoldInitializerAndIfToElvis")
        if (skipOnSdkAnnotation == null) {
            return TestMetadataResolver.Resolution.NothingToChange("SkipOnSdk annotation not found")
        }

        val currentSdk = Build.VERSION.SDK_INT

        if (skipOnSdkAnnotation.sdk.contains(currentSdk)) {
            logger.debug(
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
