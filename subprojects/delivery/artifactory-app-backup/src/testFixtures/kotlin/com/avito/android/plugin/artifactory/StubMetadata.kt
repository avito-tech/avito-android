package com.avito.android.plugin.artifactory

import okhttp3.mockwebserver.MockResponse

/**
 * В maven-publish стали проверять metadata, придется отдавать при попытках залить на mockwebserver
 * https://github.com/gradle/gradle/pull/9465
 */
public fun MockResponse.setStubMavenMetadataBody(): MockResponse {
    setBody(
        """<?xml version="1.0" encoding="UTF-8"?>
<metadata>
    <groupId>unspecified</groupId>
    <artifactId>unspecified</artifactId>
    <version>unspecified</version>
    <versioning>
        <latest>unspecified</latest>
        <release>unspecified</release>
        <versions>
            <version>unspecified</version>
        </versions>
        <lastUpdated>20170607140216</lastUpdated>
    </versioning>
</metadata>"""
    )
    return this
}
