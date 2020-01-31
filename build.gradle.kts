@file:Suppress("UnstableApiUsage")

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryPlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm") apply false
    id("com.android.application") apply false
}

val artifactoryUrl: String by project
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

    val sourcesTaskName = "sourceJar"

    plugins.matching { it is AppPlugin || it is LibraryPlugin }.whenPluginAdded {
        configure<BaseExtension> {
            sourceSets {
                named("main").configure { java.srcDir("src/main/kotlin") }
                named("androidTest").configure { java.srcDir("src/androidTest/kotlin") }
                named("test").configure { java.srcDir("src/test/kotlin") }
            }
        }
    }

    plugins.withType<LibraryPlugin> {
        tasks.named<Jar>(sourcesTaskName).configure {
            classifier = "sources"
            from(this@withType.extension.sourceSets["main"].java.srcDirs)
        }
    }

    plugins.withId("digital.wup.android-maven-publish") {
        tasks.create<Jar>(sourcesTaskName)

        //todo remove afterEvaluate if possible
        afterEvaluate {
            extensions.getByType<PublishingExtension>().run {
                publications {
                    create<MavenPublication>("mavenAar") {
                        from(components["android"])
                    }
                }
            }
        }
    }

    plugins.withType<MavenPublishPlugin> {
        extensions.getByType<PublishingExtension>().run {

            publications {
                //todo ненадежная проверка, завязана на порядок
                if (plugins.hasPlugin("kotlin") && !plugins.hasPlugin("java-gradle-plugin")) {
                    create<MavenPublication>("maven") {
                        from(components["java"])
                        afterEvaluate {
                            artifactId = this@subprojects.getOptionalExtra("artifact-id") ?: this@subprojects.name
                        }
                    }
                }

                withType<MavenPublication> {
                    if (!name.contains("pluginmarker", ignoreCase = true)) {
                        artifact(tasks.named(sourcesTaskName).get())
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
                    setUrl("$artifactoryUrl/libs-release-local")
                    credentials {
                        username = System.getenv("ARTIFACTORY_USER")
                        password = System.getenv("ARTIFACTORY_PASSWORD")
                    }
                }
            }
        }
    }

    plugins.withType<org.jetbrains.kotlin.gradle.plugin.KotlinBasePluginWrapper> {
        this@subprojects.run {
            tasks {
                withType<KotlinCompile> {
                    kotlinOptions {
                        jvmTarget = javaVersion
                        allWarningsAsErrors = false //todo we use deprecation a lot, and it's a compiler warning
                        freeCompilerArgs = freeCompilerArgs + "-Xuse-experimental=kotlin.Experimental"
                    }
                }
            }

            dependencies {
                "implementation"("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
            }
        }
    }

    plugins.withId("kotlin") {

        //todo withSourcesJar 6.0 gradle
        tasks.create<Jar>(sourcesTaskName) {
            classifier = "sources"
            from(sourceSets.main.get().allJava)
        }

        this@subprojects.tasks {
            withType<Test> {
                @Suppress("UnstableApiUsage")
                useJUnitPlatform()

                systemProperty("kotlinVersion", kotlinVersion)
                systemProperty("compileSdkVersion", compileSdkVersion)
                systemProperty("buildToolsVersion", buildToolsVersion)

                /**
                 * IDEA добавляет специальный init script, по нему понимаем что запустили в IDE
                 * используется в :test-project
                 */
                systemProperty("isInvokedFromIde",
                    gradle.startParameter.allInitScripts.find { it.name.contains("ijtestinit") } != null)
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

    tasks.withType(Test::class.java) {
        systemProperty("rootDir", "${project.rootDir}")
    }
}

val installGitHooksTask = tasks.register<Exec>("installGitHooks") {
    group = "Build Setup"
    description = "Install local repository git hooks"
    commandLine("git")
    args("config", "core.hooksPath", ".git_hooks")
}

tasks.named<Wrapper>("wrapper") {
    distributionType = Wrapper.DistributionType.BIN
    gradleVersion = project.properties["gradleVersion"] as String
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

fun Project.getOptionalExtra(key: String): String? {
    return if (extra.has(key)) {
        (extra[key] as? String)?.let { if (it.isBlank()) null else it }
    } else {
        null
    }
}
