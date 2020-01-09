plugins {
    id("kotlin")
    id("java-gradle-plugin")
    `maven-publish`
}

val kotlinVersion: String by project
val kotlinHtmlVersion: String by project
val androidGradlePluginVersion: String by project
val okhttpVersion: String by project

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("com.android.tools.build:gradle:$androidGradlePluginVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:$kotlinHtmlVersion")
    implementation("com.squareup.okhttp3:okhttp:$okhttpVersion")

    implementation(project(":android"))
    implementation(project(":utils"))
    implementation(project(":logging"))
    implementation(project(":okhttp"))
    implementation(project(":impact"))
    implementation(project(":sentry"))
    implementation(project(":git"))
    implementation(project(":bitbucket"))
    implementation(project(":kotlin-dsl-support"))

    testImplementation(testFixtures(project(":logging")))
}

gradlePlugin {
    plugins {
        create("com.avito.android.lint-report") {
            id = "com.avito.android.lint-report"
            implementationClass = "com.avito.android.lint.LintReportPlugin"
        }
    }
}

