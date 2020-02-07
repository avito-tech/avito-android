plugins {
    id("kotlin")
    id("java-gradle-plugin")
    `maven-publish`
    id("com.jfrog.bintray")
}

val funktionaleVersion: String by project
val kotlinHtmlVersion: String by project
val androidGradlePluginVersion: String by project
val okhttpVersion: String by project

dependencies {
    implementation("org.funktionale:funktionale-try:$funktionaleVersion")
    implementation("com.android.tools.build:gradle:$androidGradlePluginVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:$kotlinHtmlVersion")
    implementation("com.squareup.okhttp3:okhttp:$okhttpVersion")

    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:utils"))
    implementation(project(":subprojects:gradle:logging"))
    implementation(project(":subprojects:common:okhttp"))
    implementation(project(":subprojects:gradle:impact"))
    implementation(project(":subprojects:common:sentry"))
    implementation(project(":subprojects:gradle:git"))
    implementation(project(":subprojects:gradle:bitbucket"))
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))

    testImplementation(testFixtures(project(":subprojects:gradle:logging")))
}

gradlePlugin {
    plugins {
        create("lintReport") {
            id = "com.avito.android.lint-report"
            implementationClass = "com.avito.android.lint.LintReportPlugin"
        }
    }
}
