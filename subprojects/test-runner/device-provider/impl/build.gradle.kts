import com.avito.android.test.applyOptionalSystemProperty

plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
    id("convention.integration-testing")
}

dependencies {
    api(projects.testRunner.service)
    api(projects.testRunner.deviceProvider.api)
    api(projects.gradle.kubernetes)

    implementation(projects.gradle.process)
    implementation(projects.logger.logger)
    implementation(projects.common.result)

    integTestImplementation(projects.common.truthExtensions)

    testImplementation(testFixtures(projects.logger.logger))
    testImplementation(libs.coroutinesTest)

    testFixturesApi(testFixtures(projects.common.httpClient))
}

kotlin {
    explicitApi()
}

tasks.named<Test>("integrationTest").configure {
    applyOptionalSystemProperty("kubernetesUrl")
    applyOptionalSystemProperty("kubernetesToken")
    applyOptionalSystemProperty("kubernetesCaCertData")
    applyOptionalSystemProperty("kubernetesNamespace")
}
