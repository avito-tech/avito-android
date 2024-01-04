plugins {
   kotlin("jvm")
    `java-gradle-plugin`
}

group = "com.avito.android.buildlogic"

dependencies {
    implementation(project(":gradle-ext"))
    implementation(libs.versionsGradle)
    implementation(libs.detektGradle)
    // to access LibrariesForLibs
    // workaround for https://github.com/gradle/gradle/issues/15383
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

gradlePlugin {
    plugins {
        create("detekt") {
            id = "convention.detekt"
            implementationClass = "com.avito.DetektPlugin"
        }

        create("dependency-updates") {
            id = "convention.dependency-updates"
            implementationClass = "com.avito.DependencyUpdatesPlugin"
        }
    }
}
