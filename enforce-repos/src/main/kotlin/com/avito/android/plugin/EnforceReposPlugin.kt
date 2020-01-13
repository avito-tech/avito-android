package com.avito.android.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository

class EnforceReposPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        check(target.isRoot()) { "EnforceRepos plugin must be applied to the root project" }

        val artifactoryUrl = target.property("artifactoryUrl").toString()

        /**
         * Preserve order of repositories due to accidental wrong google-android libraries resolving in jcenter
         */
        val avitoRepos = linkedSetOf(
            "https://maven.google.com/",
            "$artifactoryUrl/google-android",
            "$artifactoryUrl/jcenter",
            "$artifactoryUrl/ext-release-local",
            "$artifactoryUrl/plugins-release-local",
            "$artifactoryUrl/libs-release-local",
            "$artifactoryUrl/intellij-repository",
            "$artifactoryUrl/jetbrains",
            "$artifactoryUrl/jitpack.io",
            "$artifactoryUrl/fabric",
            "$artifactoryUrl/repo1",
            "$artifactoryUrl/kotlin-eap",
            "$artifactoryUrl/gradle-plugins",
            "$artifactoryUrl/gradle-libs-releases",
            "$artifactoryUrl/ktor"
        )

        val registeredRepos = avitoRepos + linkedSetOf(
            "google/m2repository",
            "extras/m2repository", //google play services
            ".gradle/caches",
            ".gradle-test-kit-", //for testing build
            "gradle-user-home/caches",
            "embedded-kotlin-repo-" //for embedded in dsl kotlin plugin
        )

        val unusedRepos = linkedSetOf(
            "android/m2repository", // old android sdk-bundled repos
            "gradle/m2repository", // bundled with android studio gradle plugin, we will download it instead
            "jetbrains.com/intellij-repository" // added implicitly by idea-gradle-plugin
        )


        target.subprojects {

            val repositories = it.repositories
            /**
             * removes all dep's repositories instead of [registeredRepos]
             * Why we need this? To fulfill contract: all dependencies proxied through in-house artifactory instance
             * Sadly, we can't proxy all dependencies, because of some proprietary(google play services) and hardcode in some plugins
             */
            repositories.allMaven { repo ->
                val url = repo.url.toString()

                if (unusedRepos.find { slug -> url.contains(slug) } != null) {
                    repositories.remove(repo)
                } else if (registeredRepos.find { slug -> url.contains(slug) } == null) {
                    throw IllegalStateException(
                        "Project uses not registered maven repository: $url. " +
                            "Use artifactory instead"
                    )
                }
            }

            avitoRepos.forEach { repoUrl ->
                repositories.maven { repo ->
                    repo.setUrl(repoUrl)
                    if (repoUrl.endsWith("/google-android")) {
                        @Suppress("UnstableApiUsage")
                        repo.content {
                            it.includeGroupByRegex("android\\.arch\\..+")
                            it.includeGroupByRegex("androidx\\..+")
                            it.includeGroupByRegex("com\\.android\\..+")

                            it.includeGroup("com.google.android.gms")
                            it.includeGroup("com.google.android.play")
                            it.includeGroup("com.google.firebase")
                            it.includeGroup("com.google.android.material")
                            it.includeGroup("com.crashlytics.sdk.android")
                            it.includeGroup("io.fabric.sdk.android")
                        }
                    } else {
                        @Suppress("UnstableApiUsage")
                        repo.content {
                            it.excludeGroupByRegex("android\\.arch\\..+")
                            it.excludeGroupByRegex("androidx\\..+")
                            it.excludeGroupByRegex("com\\.android\\..+")

                            it.excludeGroup("com.google.android.gms")
                            it.excludeGroup("com.google.android.play")
                            it.excludeGroup("com.google.firebase")
                            it.excludeGroup("com.crashlytics.sdk.android")
                            it.excludeGroup("io.fabric.sdk.android")
                        }
                    }
                }
            }
        }
    }

    @Suppress("unused") //used in script
    private fun RepositoryHandler.allMaven(action: (MavenArtifactRepository) -> Unit) {
        all {
            if (this is MavenArtifactRepository) {
                action(this)
            }
        }
    }

    private fun Project.isRoot() = (project == project.rootProject)
}
