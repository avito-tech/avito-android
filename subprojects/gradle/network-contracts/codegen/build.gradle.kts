plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
    id("convention.test-fixtures")
    id("convention.kotlin-serialization")
}

dependencies {
    api(project(":subprojects:gradle:clickstream"))
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:gradle:mtls"))
    implementation(project(":subprojects:gradle:process"))
    implementation(project(":subprojects:logger:gradle-logger"))
    implementation(project(":subprojects:common:okhttp"))
    implementation(project(":subprojects:gradle:git"))
    implementation(project(":subprojects:assemble:build-verdict-tasks-api"))
    implementation(project(":subprojects:gradle:build-environment"))

    implementation(libs.okhttp)
    implementation(libs.okhttpLogging)
    implementation(libs.bundles.ktor)
    implementation(libs.kotlinGradle)

    implementation(project(":subprojects:logger:gradle-logger"))
    gradleTestImplementation(libs.jsonAssert)
    gradleTestImplementation(libs.junitJupiterParams)
    gradleTestImplementation(project(":subprojects:common:test-okhttp"))
    gradleTestImplementation(project(":subprojects:gradle:test-project"))
    gradleTestImplementation(project(":subprojects:gradle:git"))

    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.coroutinesTest)
    testFixturesImplementation(libs.kotlinx.serialization.json)
    testFixturesImplementation(testFixtures(project(":subprojects:gradle:mtls")))
}

gradlePlugin {
    plugins {
        create("contracts") {
            id = "com.avito.android.schemes-contracts"
            implementationClass = "com.avito.android.contracts.platform.ContractsModulePlugin"
            displayName = "Schemes Contracts Plugin"
        }
    }
    plugins {
        create("contractsRoot") {
            id = "com.avito.android.schemes-contracts-root"
            implementationClass = "com.avito.android.contracts.platform.ContractsRootPlugin"
            displayName = "Schemes Contracts Root Plugin"
        }
    }
    plugins {
        create("networkContracts") {
            id = "com.avito.android.network-contracts"
            implementationClass = "com.avito.android.network_contracts.NetworkContractsModulePlugin"
            displayName = "Network Contracts Plugin"
        }
    }
    plugins {
        create("networkContractsRoot") {
            id = "com.avito.android.network-contracts-root"
            implementationClass = "com.avito.android.network_contracts.NetworkContractsRootPlugin"
            displayName = "Network Contracts Root Plugin"
        }
    }
}
