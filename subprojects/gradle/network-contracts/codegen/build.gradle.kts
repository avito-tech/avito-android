plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
    id("convention.test-fixtures")
    id("convention.serialization")
}

dependencies {
    implementation(projects.subprojects.gradle.gradleExtensions)
    implementation(projects.subprojects.common.okhttp)

    implementation(libs.okhttp)
    implementation(libs.okhttpLogging)

    implementation(projects.subprojects.logger.gradleLogger)

    gradleTestImplementation(libs.jsonAssert)
    gradleTestImplementation(projects.subprojects.gradle.testProject)
}

gradlePlugin {
    plugins {
        create("networkContracts") {
            id = "com.avito.android.network_contracts.root"
            implementationClass = "com.avito.android.network_contracts.NetworkContractsRootPlugin"
            displayName = "Network Contracts Root Plugin"
        }
        create("networkContractsModule") {
            id = "com.avito.android.network_contracts.module"
            implementationClass = "com.avito.android.network_contracts.NetworkContractsModulePlugin"
            displayName = "Network Contracts Module Plugin"
        }
    }
}
