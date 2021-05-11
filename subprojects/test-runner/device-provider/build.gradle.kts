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
    api(project(":test-runner:service"))
    api(project(":test-runner:shared"))
    api(project(":gradle:runner:stub"))
    api(project(":gradle:kubernetes"))

    implementation(project(":gradle:process"))
    implementation(project(":common:logger"))
    implementation(project(":common:result"))

    integTestImplementation(project(":common:truth-extensions"))

    testImplementation(testFixtures(project(":common:logger")))
    testImplementation(libs.coroutinesTest)

    testFixturesApi(testFixtures(project(":common:http-client")))
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
