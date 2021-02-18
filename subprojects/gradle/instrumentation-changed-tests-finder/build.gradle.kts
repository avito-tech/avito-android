plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin-legacy")
}

dependencies {
    implementation(gradleApi())
    implementation(project(":subprojects:common:kotlin-ast-parser")) {
        because("Need to know all classes in files")
    }
    implementation(project(":subprojects:common:files"))
    implementation(project(":subprojects:gradle:process"))
    implementation(project(":subprojects:gradle:gradle-logger"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:gradle:instrumentation-tests-dex-loader")) {
        because("TestInApkModel")
    }
    implementation(project(":subprojects:gradle:impact-shared")) {
        because("ChangesDetector reuse")
    }

    testImplementation(testFixtures(project(":subprojects:common:logger")))
    testImplementation(project(":subprojects:gradle:git"))
    testImplementation(project(":subprojects:gradle:impact-shared-test-fixtures"))
    testImplementation(project(":subprojects:gradle:instrumentation-tests-dex-loader-test-fixtures"))
    testImplementation(project(":subprojects:gradle:test-project")) {
        because("File extensions") // todo probably move to :common:files
    }
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
