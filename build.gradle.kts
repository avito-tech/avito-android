@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl` apply false
}

val projectVersion: String by project
val buildToolsVersion: String by project
val javaVersion: String by project
val compileSdkVersion: String by project
val kotlinVersion: String by project
val junit5Version: String by project
val junit5PlatformVersion: String by project
val truthVersion: String by project

val publishToBintrayTask = tasks.register<Task>("publishToBintray") {
    group = "publication"
}

val publishToArtifactoryTask = tasks.register<Task>("publishToArtifactory") {
    group = "publication"
}

val finalProjectVersion: String = System.getenv("PROJECT_VERSION").let { env ->
    if (env.isNullOrBlank()) projectVersion else env
}

subprojects {

    repositories {
        jcenter()
        google()
    }

    group = "com.avito.android"
    version = finalProjectVersion

    plugins.withType<MavenPublishPlugin> {
        extensions.getByType<PublishingExtension>().run {

            //todo withSourcesJar 6.0 gradle
            val sourcesTask = tasks.create<Jar>("sourceJar") {
                classifier = "sources"
                from(sourceSets.main.get().allJava)
            }

            publications {
                //todo ненадежная проверка, завязана на порядок
                if (!plugins.hasPlugin("java-gradle-plugin")) {
                    create<MavenPublication>("maven") {
                        from(components["java"])
                        // вложенные модули будут представлены как dir-subdir-module
                        artifactId = path.removePrefix(":").replace(':', '-')
                    }
                }

                withType<MavenPublication> {
                    if (!name.contains("pluginmarker", ignoreCase = true)) {
                        artifact(sourcesTask)
                    }
                }
            }

            publishToBintrayTask.configure {
                dependsOn(tasks.named("publishAllPublicationsToBintrayRepository"))
            }
            publishToArtifactoryTask.configure {
                dependsOn(tasks.named("publishAllPublicationsToArtifactoryRepository"))
            }

            repositories {
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
        this@subprojects.tasks {

            withType<KotlinCompile> {
                kotlinOptions {
                    jvmTarget = javaVersion
                    allWarningsAsErrors = true
                    freeCompilerArgs =
                        freeCompilerArgs + "-Xuse-experimental=kotlin.Experimental" + "-XXLanguage:+InlineClasses"
                }
            }

            withType<Test> {
                @Suppress("UnstableApiUsage")
                useJUnitPlatform()

                systemProperty("kotlinVersion", kotlinVersion)
                systemProperty("compileSdkVersion", compileSdkVersion)
                systemProperty("buildToolsVersion", buildToolsVersion)
            }
        }

        dependencies {
            "testImplementation"("org.junit.jupiter:junit-jupiter-api:${junit5Version}")

            "testRuntimeOnly"("org.junit.platform:junit-platform-runner:$junit5PlatformVersion")
            "testRuntimeOnly"("org.junit.platform:junit-platform-launcher:$junit5PlatformVersion")
            "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:$junit5Version")

            "testImplementation"(gradleTestKit())
            "testImplementation"("com.google.truth:truth:$truthVersion")
        }
    }

    plugins.withId("java-test-fixtures") {

        dependencies {
            "testFixturesImplementation"("org.junit.jupiter:junit-jupiter-api:${junit5Version}")
            "testFixturesImplementation"("com.google.truth:truth:$truthVersion")
        }
    }
}

val installGitHooksTask = tasks.register<Exec>("installGitHooks") {
    group = "Build Setup"
    description = "Install local repository git hooks"
    commandLine("git")
    args("config", "core.hooksPath", ".git_hooks")
}

tasks {
    wrapper {
        distributionType = Wrapper.DistributionType.BIN
        gradleVersion = project.properties["gradleVersion"] as String
    }
}

project.gradle.startParameter.run { setTaskNames(taskNames + ":${installGitHooksTask.name}") }

fun <T> NamedDomainObjectCollection<T>.namedOrNull(name: String): NamedDomainObjectProvider<T>? {
    return try {
        named(name)
    } catch (e: UnknownDomainObjectException) {
        null
    }
}

val Project.sourceSets: SourceSetContainer
    get() = (this as ExtensionAware).extensions.getByName("sourceSets") as SourceSetContainer

val SourceSetContainer.main: NamedDomainObjectProvider<SourceSet>
    get() = named<SourceSet>("main")
