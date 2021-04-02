import com.avito.android.test.applyOptionalSystemProperty

plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
    id("convention.integration-testing")
    id("convention.libraries")
}

publish {
    artifactId.set("runner-device-provider")
}

dependencies {
    api(projects.gradle.runner.service)
    api(projects.gradle.runner.shared)
    api(projects.gradle.runner.stub)
    api(projects.gradle.kubernetes)

    implementation(projects.gradle.process)
    implementation(projects.common.logger)
    implementation(projects.common.result)

    integTestImplementation(projects.common.truthExtensions)

    testImplementation(testFixtures(projects.common.logger))
    testImplementation(libs.coroutinesTest)
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
