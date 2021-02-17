plugins {
    `maven-publish`
}

group = "com.avito.android"

// avito.project.version override from CI property, looks like it could be simplified to single one
@Suppress("UnstableApiUsage")
version = providers.systemProperty("avito.project.version").forUseAtConfigurationTime()
    .orElse(providers.gradleProperty("projectVersion").forUseAtConfigurationTime())
    .get()

publishing.publications.withType<MavenPublication> {
    pom {
        name.set("Avito Android Infrastructure")
        description.set("Collection of infrastructure libraries and gradle plugins of Avito Android project")
        url.set("https://github.com/avito-tech/avito-android")

        scm {
            url.set("https://github.com/avito-tech/avito-android")
        }
        licenses {
            license {
                name.set("MIT License")
                url.set("https://github.com/avito-tech/avito-android/blob/develop/LICENSE")
            }
        }
        developers {
            developer {
                id.set("dsvoronin")
                name.set("Dmitriy Voronin")
                url.set("https://github.com/dsvoronin")
            }
            developer {
                id.set("eugene-krivobokov")
                name.set("Eugene Krivobokov")
                url.set("https://github.com/eugene-krivobokov")
            }
            developer {
                id.set("sboishtyan")
                name.set("Sergey Boishtyan")
                url.set("https://github.com/sboishtyan")
            }
        }
    }
}
