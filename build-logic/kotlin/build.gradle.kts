plugins {
    kotlin("jvm")
    `java-gradle-plugin`
}

group = "com.avito.android.buildlogic"

dependencies {
    implementation(project(":gradle-ext"))
    implementation(project(":testing"))
    implementation(project(":dependency-locking"))
    implementation(project(":checks"))
    implementation(libs.kotlinGradle)
    implementation(libs.kotlinx.serialization.gradle)
    implementation(libs.kspGradle)
    // workaround for https://github.com/gradle/gradle/issues/15383
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

gradlePlugin {
    plugins {
        create("kotlin-jvm") {
            id = "convention.kotlin-jvm"
            implementationClass = "com.avito.jvm.KotlinJvmPlugin"
        }

        create("kotlin-jvm-android") {
            id = "convention.kotlin-jvm-android"
            implementationClass = "com.avito.jvm.KotlinJvmAndroidPlugin"
        }

        create("ksp") {
            id = "convention.ksp"
            implementationClass = "com.avito.KspPlugin"
        }

        create("kotlin-serialization") {
            id = "convention.kotlin-serialization"
            implementationClass = "com.avito.KotlinSerializationPlugin"
        }
    }
}
