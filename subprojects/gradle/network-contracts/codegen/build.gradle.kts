plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
    id("convention.test-fixtures")
    id("convention.serialization")
}

dependencies {
    implementation(projects.subprojects.gradle.git)
    implementation(projects.subprojects.gradle.gradleExtensions)
    implementation(projects.subprojects.gradle.mtls)
    implementation(projects.subprojects.gradle.process)
    implementation(projects.subprojects.logger.gradleLogger)
    implementation(projects.subprojects.common.okhttp)
    implementation(projects.subprojects.assemble.buildVerdictTasksApi)

    implementation(libs.okhttp)
    implementation(libs.okhttpLogging)
    implementation(libs.bundles.ktor)

    implementation(projects.subprojects.logger.gradleLogger)

    gradleTestImplementation(libs.jsonAssert)
    gradleTestImplementation(projects.subprojects.common.testOkhttp)
    gradleTestImplementation(projects.subprojects.gradle.testProject)

    testImplementation(libs.mockitoKotlin)
    testFixturesImplementation(testFixtures(projects.subprojects.gradle.mtls))
}

gradlePlugin {
    plugins {
        create("networkContracts") {
            id = "com.avito.android.network-contracts"
            implementationClass = "com.avito.android.network_contracts.NetworkContractsPlugin"
            displayName = "Network Contracts Plugin"
        }
    }
}
