??? note "Setup plugins"
    In the `settings.gradle`:

    ```groovy
    pluginManagement {
        repositories {
            maven {
                name = "Avito bintray"
                url = "https://dl.bintray.com/avito/maven"
            }
        }
        resolutionStrategy {
            eachPlugin {
                String pluginId = requested.id.id
                if (pluginId.startsWith("com.avito.android")) {
                    def artifact = pluginId.replace("com.avito.android.", "")
                    useModule("com.avito.android:$artifact:$avitoToolsVersion")
                }
            }
        }
    }
    ```
    
    `avitoToolsVersion` could be exact version, or property in project's `gradle.properties`.\
    The latest version could be found on project's [release page](https://github.com/avito-tech/avito-android/releases).
