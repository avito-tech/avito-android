plugins {
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

val kotlinVersion: String by project
val antPatternMatcherVersion: String by project
val mockitoKotlinVersion: String by project

dependencies {
    implementation(gradleApi())
    implementation(project(":subprojects:gradle:impact-shared"))
    implementation(project(":subprojects:gradle:utils"))
    implementation(project(":subprojects:gradle:logging"))
    implementation(project(":subprojects:gradle:files"))
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:git"))
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))
    implementation("io.github.azagniotov:ant-style-path-matcher:$antPatternMatcherVersion")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")

    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(testFixtures(project(":subprojects:gradle:logging")))
    testImplementation(testFixtures(project(":subprojects:gradle:git")))
    testImplementation("com.nhaarman:mockito-kotlin:$mockitoKotlinVersion")
}

gradlePlugin {
    plugins {
        create("impact") {
            id = "com.avito.android.impact"
            implementationClass = "com.avito.impact.plugin.ImpactAnalysisPlugin"
            displayName = "Impact analysis"
        }
    }
}
