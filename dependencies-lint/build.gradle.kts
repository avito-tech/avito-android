plugins {
    id("kotlin")
    id("java-gradle-plugin")
    `maven-publish`
}

val kotlinVersion: String by project
val androidGradlePluginVersion: String by project
val mockitoKotlinVersion: String by project

dependencies {
    implementation(project(":cicd"))
    implementation(project(":impact"))
    implementation(project(":files"))
    implementation(project(":utils"))
    implementation(project(":android"))
    implementation(project(":kotlin-dsl-support"))

    implementation("com.android.tools.build:gradle:$androidGradlePluginVersion")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("org.ow2.asm:asm:7.1")

    testImplementation(project(":test-project"))
    testImplementation("com.nhaarman:mockito-kotlin:$mockitoKotlinVersion")
}

gradlePlugin {
    plugins {
        create("dependenciesLintPlugin") {
            id = "com.avito.android.dependencies-lint"
            implementationClass = "com.avito.android.lint.DependenciesLintPlugin"
        }
    }
}
