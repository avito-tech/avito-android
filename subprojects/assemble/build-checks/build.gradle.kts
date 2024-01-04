plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(project(":subprojects:gradle:build-failer"))
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:impact-shared"))
    implementation(project(":subprojects:gradle:build-environment"))
    implementation(project(":subprojects:common:files"))
    implementation(project(":subprojects:common:result"))
    implementation(project(":subprojects:gradle:statsd-config"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(libs.kotlinGradle)
    implementation(libs.layoutLibApi) {
        because("com.android.resources.ResourceType")
    }
    implementation(libs.androidSdkCommon) {
        because("com.android.ide.common.symbols.SymbolTable")
    }

    // for Gradle tests
    // TODO: find a way to provide external plugins only to Gradle tests.
    //      One probable workaround to avoid this dependency at all: MBS-12338
    implementation(project(":subprojects:gradle:impact"))

    gradleTestImplementation(project(":subprojects:gradle:test-project"))
}

gradlePlugin {
    plugins {
        create("buildChecks") {
            id = "com.avito.android.build-checks"
            implementationClass = "com.avito.android.build_checks.BuildParamCheckPlugin"
            displayName = "Build checks"
        }
    }
}
