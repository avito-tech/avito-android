package com.avito;

import com.avito.ExclusiveContent.Filters;
import com.avito.ExclusiveContent.ForRepositories;

import org.gradle.api.Plugin;
import org.gradle.api.artifacts.ArtifactRepositoryContainer;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.initialization.Settings;

import java.util.List;

import static com.avito.SettingsExt.booleanProperty;

public class DependencyResolutionPlugin implements Plugin<Settings> {

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void apply(Settings settings) {
        final var extra = settings.getExtensions().getExtraProperties();
        final var artifactoryUrl = ((String) extra.get("artifactoryUrl"));
        final boolean isInternalBuild = booleanProperty(settings, "avito.internalBuild", false);
        settings.dependencyResolutionManagement(dependencyResolutionManagement -> {
            dependencyResolutionManagement.repositories(artifactRepositories -> {
                final var exclusiveContents = List.of(
                    new ExclusiveContent(
                        new ForRepositories(List.of(
                            new MavenProxyRepository(
                                artifactRepositories,
                                artifactoryUrl,
                                "libs-release-local"
                            )
                        )),
                        new Filters(
                            List.of(
                                new IncludeModuleByRegex("com\\.avito\\.android", ".*")
                            )
                        )
                    ),
                    new ExclusiveContent(
                        new ForRepositories(
                            List.of(MavenRepositoryFactory.createFactory(
                                artifactRepositories,
                                artifactoryUrl,
                                "gradle-plugins",
                                "https://plugins.gradle.org/m2/"
                            ))
                        ),
                        new Filters(
                            List.of(
                                new IncludeModule("com.github.ben-manes", "gradle-versions-plugin"),
                                new IncludeModule("org.gradle", "test-retry-gradle-plugin")
                            )
                        )
                    ),
                    new ExclusiveContent(
                        new ForRepositories(
                            List.of(MavenRepositoryFactory.createFactory(
                                artifactRepositories,
                                artifactoryUrl,
                                "google-android",
                                "https://dl.google.com/dl/android/maven2/"
                            ))
                        ),
                        new Filters(
                            List.of(
                                new IncludeModuleByRegex("com\\.android.*", "(?!r8).*"),
                                new IncludeModuleByRegex("com\\.google\\.android.*", "(?!annotations).*"),
                                new IncludeGroupByRegex("androidx\\..*"),
                                new IncludeGroup("com.google.testing.platform")
                            )
                        )
                    ),
                    new ExclusiveContent(
                        new ForRepositories(
                            List.of(MavenRepositoryFactory.createFactory(
                                artifactRepositories,
                                artifactoryUrl,
                                "jitpack.io",
                                "https://jitpack.io"
                            ))
                        ),
                        new Filters(
                            List.of(
                                new IncludeModule("com.github.fkorotkov", "k8s-kotlin-dsl"),
                                new IncludeModule("com.github.tiann", "FreeReflection")
                            )
                        )
                    ),
                    new ExclusiveContent(
                        new ForRepositories(
                            List.of(MavenRepositoryFactory.createFactory(
                                artifactRepositories,
                                artifactoryUrl,
                                "jcenter",
                                "https://jcenter.bintray.com"
                            ))
                        ),
                        new Filters(
                            List.of(
                                new IncludeGroup("org.jetbrains.trove4j"),
                                new IncludeModule("org.jetbrains.teamcity", "teamcity-rest-client")
                            )
                        )
                    ),
                    new ExclusiveContent(
                        new ForRepositories(
                            List.of(MavenRepositoryFactory.createFactory(
                                artifactRepositories,
                                artifactoryUrl,
                                "r8-releases",
                                "https://storage.googleapis.com/r8-releases/raw"
                            ))
                        ),
                        new Filters(
                            List.of(
                                new IncludeModule("com.android.tools", "r8")
                            )
                        )
                    )
                );
                exclusiveContents.forEach(
                    exclusiveContent -> exclusiveContent.apply(artifactRepositories)
                );
                MavenRepositoryFactory.createFactory(
                    artifactRepositories,
                    artifactoryUrl,
                    "mavenCentral",
                    "https://repo1.maven.org/maven2"
                ).create();
                if (isInternalBuild) {
                    artifactRepositories.withType(MavenArtifactRepository.class).forEach(maven -> {
                        if (!ArtifactRepositoryContainer.DEFAULT_MAVEN_LOCAL_REPO_NAME.equals(maven.getName()) &&
                            !maven.getUrl().toString().startsWith(artifactoryUrl)) {
                            throw new IllegalStateException(
                                "Unexpected maven repository: name" + maven.getName() + ", url=" + maven.getUrl() + "\n" +
                                    "You should use proxy repository in " + artifactoryUrl + "\n" +
                                    "If this is technically impossible (init scripts and so on), \n" +
                                    "add this repository to the exclusions in this check.\n"
                            );
                        }
                    });
                }
            });
        });
    }
}
