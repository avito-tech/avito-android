plugins {
    kotlin("jvm") version "1.3.61"
    `maven-publish`
}

val compileSdkVersion: String by project
val kotlinVersion: String by project
val junit5Version: String by project
val junit5PlatformVersion: String by project
val truthVersion: String by project

val projectVersion = "1-test-8"

allprojects {
    repositories {
        jcenter()
        google()
    }
}

subprojects {

    group = "com.avito.android"
    version = projectVersion

    plugins.withType<MavenPublishPlugin> {
        extensions.getByType<PublishingExtension>().run {
            this.repositories {
                maven {
                    name = "bintray"
                    val bintrayUsername = "avito-tech"
                    val bintrayRepoName = "maven"
                    val bintrayPackageName = "avito-android"
                    setUrl("https://api.bintray.com/maven/$bintrayUsername/$bintrayRepoName/$bintrayPackageName/;publish=0")
                    credentials {
                        username = System.getenv("BINTRAY_USER")
                        password = System.getenv("BINTRAY_API_KEY")
                    }
                }

                maven {
                    name = "artifactory"
                    val artifactoryUrl = System.getenv("ARTIFACTORY_URL")
                    setUrl(artifactoryUrl)
                    credentials {
                        username = System.getenv("ARTIFACTORY_USER")
                        password = System.getenv("ARTIFACTORY_PASSWORD")
                    }
                }
            }
        }
    }

    plugins.withId("kotlin") {
        java {
            @Suppress("UnstableApiUsage")
            withSourcesJar()
        }

        this@subprojects.tasks {

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
                @Suppress("UnstableApiUsage")
                useJUnitPlatform()

                systemProperty("kotlinVersion", kotlinVersion)
                systemProperty("compileSdkVersion", compileSdkVersion)
            }
        }

        dependencies {
            testImplementation("org.junit.jupiter:junit-jupiter-api:${junit5Version}")

            testRuntimeOnly("org.junit.platform:junit-platform-runner:$junit5PlatformVersion")
            testRuntimeOnly("org.junit.platform:junit-platform-launcher:$junit5PlatformVersion")
            testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junit5Version")

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

tasks {
    wrapper {
        distributionType = Wrapper.DistributionType.BIN
        gradleVersion = "6.0.1"
    }
}
