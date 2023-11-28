plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
    id("convention.test-fixtures")
    id("convention.serialization")
}

dependencies {
    implementation(projects.subprojects.gradle.gradleExtensions)
    implementation(projects.subprojects.gradle.mtls)
    implementation(projects.subprojects.common.okhttp)

    implementation(libs.okhttp)
    implementation(libs.okhttpLogging)
    implementation(libs.bundles.ktor)

    implementation(projects.subprojects.logger.gradleLogger)

    gradleTestImplementation(libs.jsonAssert)
    gradleTestImplementation(projects.subprojects.common.testOkhttp)
    gradleTestImplementation(projects.subprojects.gradle.testProject)

    testFixturesImplementation(testFixtures(projects.subprojects.gradle.mtls))
}

gradlePlugin {
    plugins {
        create("networkContractsModule") {
            id = "com.avito.android.network-contracts"
            implementationClass = "com.avito.android.network_contracts.NetworkContractsModulePlugin"
            displayName = "Network Contracts Module Plugin"
        }
    }
}
