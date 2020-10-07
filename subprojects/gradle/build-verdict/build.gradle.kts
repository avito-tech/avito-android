plugins {
    id("kotlin")
    id("java-gradle-plugin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(gradleApi())
    implementation(Dependencies.gradle.kotlinPlugin)
    implementation(Dependencies.gradle.androidPlugin) {
        because("Ad-hoc TODO add documentation MBS-9695")
    }
    implementation(project(":common:throwable-utils"))
    implementation(project(":gradle:kotlin-dsl-support"))
    implementation(project(":gradle:ci-logger"))
    implementation(Dependencies.gson)
    testImplementation(project(":gradle:test-project"))
}

gradlePlugin {
    plugins {
        create("buildVerdict") {
            id = "com.avito.android.build-verdict"
            implementationClass = "com.avito.android.build_verdict.BuildVerdictPlugin"
            displayName = "Create file with a build verdict"
        }
    }
}
