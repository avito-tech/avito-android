plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(gradleApi())
    implementation(project(":subprojects:common:kotlin-ast-parser")) {
        because("Need to know all classes in files")
    }
    implementation(project(":subprojects:common:files"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:gradle:worker-extensions"))
    implementation(project(":subprojects:gradle:impact-shared")) {
        because("ChangesDetector reuse")
    }
    implementation(project(":subprojects:logger:gradle-logger"))

    gradleTestImplementation(project(":subprojects:gradle:test-project"))
    gradleTestImplementation(project(":subprojects:gradle:git"))
    gradleTestImplementation(project(":subprojects:logger:logger"))
}

gradlePlugin {
    plugins {
        create("instrumentationModifiedTestsFinder") {
            id = "com.avito.android.instrumentation-changed-tests-finder"
            implementationClass = "com.avito.android.ChangedTestsFinderPlugin"
            displayName = "Instrumentation changed tests finder"
        }
    }
}
