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
    api(project(":gradle:runner:service"))
    api(project(":gradle:runner:shared"))
    api(project(":gradle:runner:stub"))
    api(project(":gradle:kubernetes"))

    implementation(libs.funktionaleTry)
    implementation(project(":gradle:process"))
    implementation(project(":common:logger"))

    integTestImplementation(project(":common:truth-extensions"))

    testImplementation(testFixtures(project(":common:logger")))
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
