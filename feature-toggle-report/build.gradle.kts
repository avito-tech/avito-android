plugins {
    id("kotlin")
    id("java-gradle-plugin")
    `maven-publish`
}

val kotlinVersion: String by project
val androidGradlePluginVersion: String by project
val mockitoKotlinVersion: String by project
val mockitoJunit5Version: String by project

dependencies {
    implementation(project(":utils"))
    implementation(project(":kotlin-dsl-support"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("com.android.tools.build:gradle:$androidGradlePluginVersion")

    testImplementation("com.nhaarman:mockito-kotlin:$mockitoKotlinVersion")
    testImplementation("org.mockito:mockito-junit-jupiter:$mockitoJunit5Version")
}

gradlePlugin {
    plugins {
        create("featureTogglesReport") {
            id = "com.avito.android.feature-toggles"
            implementationClass = "com.avito.android.plugin.FeatureTogglesPlugin"
        }
    }
}
