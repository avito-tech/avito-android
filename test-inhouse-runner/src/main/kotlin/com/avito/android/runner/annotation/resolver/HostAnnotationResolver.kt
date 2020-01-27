package com.avito.android.runner.annotation.resolver

import com.avito.android.runner.annotation.Host

class HostAnnotationResolver : AnnotationResolver<Host>(
    KEY,
    Host::class.java,
    { annotation -> TestMetadataResolver.Resolution.ReplaceString(annotation.apiUrl) }
) {

    companion object {
        const val KEY = "hostAnnotationApiUrl"
    }
}
