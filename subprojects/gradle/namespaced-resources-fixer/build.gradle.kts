plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":gradle:utils"))
    implementation(project(":gradle:logging"))
    implementation(project(":gradle:android"))
    implementation(project(":gradle:impact"))
    implementation(project(":gradle:files"))
    implementation(project(":gradle:kotlin-dsl-support"))

    implementation(Dependencies.gradle.kotlinPlugin)
    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.detektParser)
    implementation(Dependencies.detektCli)
    implementation(Dependencies.gradle.androidPlugin)

    testImplementation(project(":gradle:test-project"))
}

gradlePlugin {
    plugins {
        create("namespacedResourcesFixer") {
            id = "com.avito.android.namespaced-resources-fixer"
            implementationClass = "com.avito.android.plugin.NamespacedResourcesFixerPlugin"
            displayName = "Namespaced resources fixer plugin"
        }
    }
}
