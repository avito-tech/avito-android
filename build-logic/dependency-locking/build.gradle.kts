plugins {
    kotlin("jvm")
    `java-gradle-plugin`
}

group = "com.avito.android.buildlogic"

dependencies {
    implementation(projects.gradleExt)
    implementation(libs.dependencyGuard)
    // to access LibrariesForLibs
    // workaround for https://github.com/gradle/gradle/issues/15383
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

gradlePlugin {
    plugins {
        create("dependency-locking-kotlin") {
            id = "convention.dependency-locking-kotlin"
            implementationClass = "com.avito.DependencyLockingKotlin"
        }
        create("dependency-locking-android-lib") {
            id = "convention.dependency-locking-android-lib"
            implementationClass = "com.avito.DependencyLockingAndroidLib"
        }
        create("dependency-locking-android-app") {
            id = "convention.dependency-locking-android-app"
            implementationClass = "com.avito.DependencyLockingAndroidApp"
        }
    }
}
