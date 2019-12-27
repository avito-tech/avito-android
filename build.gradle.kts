plugins {
    kotlin("jvm") version "1.3.61"
}

group = "com.avito"
version = "1"

val kotlinVersion: String by project
val junit5Version: String by project
val junit5PlatformVersion: String by project
val truthVersion: String by project

allprojects {
    repositories {
        jcenter()
        google()
    }

    plugins.withId("kotlin") {

        tasks {
            compileKotlin {
                kotlinOptions.jvmTarget = "1.8"

                kotlinOptions {
                    allWarningsAsErrors = true
                    freeCompilerArgs = freeCompilerArgs + "-Xuse-experimental=kotlin.Experimental"
                }
            }

            compileTestKotlin {
                kotlinOptions.jvmTarget = "1.8"
            }

            withType<Test> {
                useJUnitPlatform()

                systemProperty("kotlinVersion", kotlinVersion)
            }
        }

        dependencies {
            testImplementation("org.junit.jupiter:junit-jupiter-api:${junit5Version}")

            testRuntime("org.junit.platform:junit-platform-runner:$junit5PlatformVersion")
            testRuntime("org.junit.platform:junit-platform-launcher:$junit5PlatformVersion")
            testRuntime("org.junit.jupiter:junit-jupiter-engine:$junit5Version")

            testImplementation(gradleTestKit())
            testImplementation("com.google.truth:truth:$truthVersion")
        }
    }

    plugins.withId("java-test-fixtures") {

        dependencies {
            "testFixturesImplementation"("org.junit.jupiter:junit-jupiter-api:${junit5Version}")
            "testFixturesImplementation"("com.google.truth:truth:$truthVersion")
        }
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

tasks {
    wrapper {
        distributionType = Wrapper.DistributionType.BIN
        gradleVersion = "5.6.4"
    }
}
