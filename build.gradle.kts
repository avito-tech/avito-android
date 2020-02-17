@file:Suppress("UnstableApiUsage")

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryPlugin
import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.BintrayExtension.PackageConfig
import com.jfrog.bintray.gradle.BintrayExtension.VersionConfig
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    val r8Version: String by project
    dependencies {
        classpath("com.android.tools:r8:$r8Version")
    }
}

plugins {
    id("org.jetbrains.kotlin.jvm") apply false
    id("com.android.application") apply false
    id("com.jfrog.bintray") version "1.8.4" apply false
}

val artifactoryUrl: String? by project
val projectVersion: String by project
val buildToolsVersion: String by project
val javaVersion: String by project
val compileSdkVersion: String by project
val kotlinVersion: String by project
val junit5Version: String by project
val junit5PlatformVersion: String by project
val truthVersion: String by project
val buildTools = requireNotNull(project.properties["buildToolsVersion"]).toString()
val compileSdk = requireNotNull(project.properties["compileSdkVersion"]).toString().toInt()
val targetSdk = requireNotNull(project.properties["targetSdkVersion"]).toString()
val minSdk = requireNotNull(project.properties["minSdkVersion"]).toString()

val publishToArtifactoryTask = tasks.register<Task>("publishToArtifactory") {
    group = "publication"
}

val publishReleaseTaskName = "publishRelease"

val finalProjectVersion: String = System.getenv("PROJECT_VERSION").let { env ->
    if (env.isNullOrBlank()) projectVersion else env
}

subprojects {

    repositories {
        jcenter()
        exclusiveContent {
            forRepository {
                google()
            }
            filter {
                includeModuleByRegex("com\\.android.*", "(?!r8).*")
                includeModuleByRegex("com\\.google\\.android.*", ".*")
                includeGroupByRegex("androidx\\..*")
            }
        }
        exclusiveContent {
            forRepository {
                maven {
                    name = "R8 releases"
                    setUrl("http://storage.googleapis.com/r8-releases/raw")
                }
            }
            filter {
                includeModule("com.android.tools", "r8")
            }
        }
    }

    group = "com.avito.android"
    version = finalProjectVersion

    /**
     * https://www.jetbrains.com/help/teamcity/build-script-interaction-with-teamcity.html#BuildScriptInteractionwithTeamCity-ReportingBuildNumber
     */
    val teamcityPrintVersionTask = tasks.register("teamcityPrintReleasedVersion") {
        group = "publication"
        description = "Prints teamcity service message to display released version as build number"

        doLast {
            logger.lifecycle("##teamcity[buildNumber '$finalProjectVersion']")
        }
    }

    tasks.register(publishReleaseTaskName) {
        group = "publication"
        finalizedBy(teamcityPrintVersionTask)
    }

    val sourcesTaskName = "sourcesJar"

    plugins.matching { it is AppPlugin || it is LibraryPlugin }.whenPluginAdded {
        configure<BaseExtension> {
            sourceSets {
                named("main").configure { java.srcDir("src/main/kotlin") }
                named("androidTest").configure { java.srcDir("src/androidTest/kotlin") }
                named("test").configure { java.srcDir("src/test/kotlin") }
            }

            buildToolsVersion(buildTools)
            compileSdkVersion(compileSdk)

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_1_8
                targetCompatibility = JavaVersion.VERSION_1_8
            }

            defaultConfig {
                minSdkVersion(minSdk)
                targetSdkVersion(targetSdk)
            }

            lintOptions {
                isAbortOnError = false
                isWarningsAsErrors = true
                textReport = true
            }
        }
    }

    plugins.withType<LibraryPlugin> {
        tasks.register<Jar>(sourcesTaskName).configure {
            archiveClassifier.set("sources")
            from(this@withType.extension.sourceSets["main"].java.srcDirs)
        }
    }

    plugins.withType<JavaGradlePluginPlugin> {
        extensions.getByType<GradlePluginDevelopmentExtension>().run {
            isAutomatedPublishing = false
        }
    }

    plugins.withId("digital.wup.android-maven-publish") {
        //todo remove afterEvaluate if possible
        afterEvaluate {

            val publicationName = "mavenAar"

            extensions.getByType<PublishingExtension>().run {
                publications {
                    create<MavenPublication>(publicationName) {
                        from(components["android"])
                        artifact(tasks.named(sourcesTaskName).get())
                    }
                }
            }

            configureBintray(publicationName)
        }
    }

    plugins.withType<MavenPublishPlugin> {
        extensions.getByType<PublishingExtension>().run {

            publications {
                //todo should not depend on ordering
                if (plugins.hasPlugin("kotlin")) {
                    val publicationName = "maven"

                    create<MavenPublication>(publicationName) {
                        from(components["java"])
                        afterEvaluate {
                            artifactId = this@subprojects.getOptionalExtra("artifact-id") ?: this@subprojects.name
                        }
                    }

                    afterEvaluate {
                        configureBintray(publicationName)
                    }
                }
            }

            repositories {
                if (!artifactoryUrl.isNullOrBlank()) {
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

        if (!artifactoryUrl.isNullOrBlank()) {
            publishToArtifactoryTask.configure {
                dependsOn(tasks.named("publishAllPublicationsToArtifactoryRepository"))
            }
        }
    }

    plugins.withType<KotlinBasePluginWrapper> {
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

        extensions.getByType<JavaPluginExtension>().run {
            withSourcesJar()
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
                 * используется в `:test-project`
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

    tasks.withType<Test> {
        systemProperty("rootDir", "${project.rootDir}")
    }
}

val installGitHooksTask = tasks.register<Exec>("installGitHooks") {
    group = "Build Setup"
    description = "Install local repository git hooks"
    commandLine("git")
    args("config", "core.hooksPath", ".git_hooks")
}

tasks.withType<Wrapper> {
    //sources unavailable with BIN until https://youtrack.jetbrains.com/issue/IDEA-231667 resolved
    distributionType = Wrapper.DistributionType.ALL
    gradleVersion = project.properties["gradleVersion"] as String
}

project.gradle.startParameter.run { setTaskNames(taskNames + ":${installGitHooksTask.name}") }

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

fun Project.configureBintray(vararg publications: String) {
    extensions.findByType<BintrayExtension>()?.run {

        //todo fail fast with meaningful error message
        user = System.getenv("BINTRAY_USER")
        key = System.getenv("BINTRAY_API_KEY")

        setPublications(*publications)

        dryRun = false
        publish = true
        pkg(closureOf<PackageConfig> {
            repo = "maven"
            userOrg = "avito-tech"
            name = "avito-android"

            version(closureOf<VersionConfig> {
                name = finalProjectVersion
            })
        })
    }

    tasks.named(publishReleaseTaskName).configure {
        dependsOn(tasks.named("bintrayUpload"))
    }
}
