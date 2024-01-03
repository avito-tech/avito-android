plugins {
    kotlin("jvm")
    `java-gradle-plugin`
}

group = "com.avito.android.buildlogic"

dependencies {
    implementation(projects.gradleExt)
    implementation(libs.kotlinGradle)
    // workaround for https://github.com/gradle/gradle/issues/15383
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

gradlePlugin {
    plugins {
        create("gradle-testing") {
            id = "convention.gradle-testing"
            implementationClass = "com.avito.GradleTestingPlugin"
        }

        create("test-fixtures") {
            id = "convention.test-fixtures"
            implementationClass = "com.avito.TestFixturesPlugin"
        }
        create("unit-testing") {
            id = "convention.unit-testing"
            implementationClass = "com.avito.UnitTestingPlugin"
        }
    }
}
