plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
}

val kotlinVersion: String by project
val antPatternMatcherVersion: String by project
val mockitoKotlinVersion: String by project

dependencies {
    implementation(gradleApi())
    implementation(project(":impact"))
    implementation(project(":utils"))
    implementation(project(":logging"))
    implementation(project(":android"))
    implementation(project(":git"))
    implementation(project(":kotlin-dsl-support"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("io.github.azagniotov:ant-style-path-matcher:$antPatternMatcherVersion")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")

    testImplementation(project(":test-project"))
    testImplementation(testFixtures(project(":logging")))
    testImplementation(testFixtures(project(":git")))
    testImplementation("com.nhaarman:mockito-kotlin:$mockitoKotlinVersion")
}

gradlePlugin {
    plugins {
        create("impact") {
            id = "com.avito.android.impact"
            implementationClass = "com.avito.impact.plugin.ImpactAnalysisPlugin"
        }
    }
}
