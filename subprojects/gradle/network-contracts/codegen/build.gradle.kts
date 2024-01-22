plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
    id("convention.test-fixtures")
    id("convention.kotlin-serialization")
}

dependencies {
    implementation(project(":subprojects:gradle:git"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:gradle:mtls"))
    implementation(project(":subprojects:gradle:process"))
    implementation(project(":subprojects:logger:gradle-logger"))
    implementation(project(":subprojects:common:okhttp"))
    implementation(project(":subprojects:assemble:build-verdict-tasks-api"))

    implementation(libs.okhttp)
    implementation(libs.okhttpLogging)
    implementation(libs.bundles.ktor)

    implementation(project(":subprojects:logger:gradle-logger"))

    gradleTestImplementation(libs.jsonAssert)
    gradleTestImplementation(libs.junitJupiterParams)
    gradleTestImplementation(project(":subprojects:common:test-okhttp"))
    gradleTestImplementation(project(":subprojects:gradle:test-project"))

    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.coroutinesTest)
    testFixturesImplementation(testFixtures(project(":subprojects:gradle:mtls")))
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
