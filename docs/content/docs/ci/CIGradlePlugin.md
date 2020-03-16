---
title: CI Gradle Plugin
type: docs
---

# CI Gradle plugin

Для настройки CI под конкретный Gradle модуль используется in-house плагин.

Решает задачу точечной настройки типа и строгости проверок для разных сценариев.

```groovy
plugins {
    id("com.avito.android.cd")
}

builds {
   ...
}
```

Применяется например в модуле приложения, однако нет никаких ограничений, чтобы применить плагин как-то иначе (см. `./build.gradle`)

## Builds

Используется набор захардкоженых сценариев(builds):

```groovy
builds {
    fastCheck {
       ...
    }
}
```

- localCheck - проверки компиляции на локальной машине
- fastCheck - максимально быстрые проверки, выполняющие [требования к CI]({{< ref "/docs/ci/CIValues.md" >}})
- fullCheck - максимально полные проверки, можем жертвовать скоростью
- release - набор действий необходимых для предоставления всех релизных артефактов

## Steps

Step is a declaration to run some logic. It works inside a scenario:

```groovy
fastCheck { // <--- scenario (build)
    unitTests {} // <--- step
    uiTests {}
    lint {}
}
```

### Built-in steps

#### UI tests

Runs instrumentation tests.

```groovy
uiTests {
  configurations = ["configurationName"] // list of instrumentation configuration to depends on
  sendStatistics = false // by default
  suppressFailures = false // by default
  useImpactAnalysis = false // by default
  suppressFlaky = false // by default. [игнорирование падений FlakyTest]({{< ref "/docs/test/FlakyTests.md" >}}).
}
```

#### Performance tests

Runs performance tests.

```groovy
performanceTests {
  configuration = "configuration name" // performance configuration from Instrumentation plugin
  enabled = true // true by default
}
```

#### Android lint

Run [Android lint]({{< ref "/docs/checks/AndroidLint.md" >}}) tasks.

```groovy
lint {}
```

#### Compile UI tests

Compile instrumentation tests. It is helpful in local development.

```groovy
compileUiTests {}
```

#### Unit tests

Run unit tests.

```groovy
unitTests {}
```

#### Upload to QApps

{{<avito step>}}

Upload [artifacts]({{< relref "#collecting-artifacts">}}) to QApps (internal system)

```groovy
artifacts {
    apk("debugApk", ...)
}
uploadToQapps {
    artifacts = ["debugApk"]
}
```

#### Upload to Artifactory

Upload [artifacts]({{< relref "#collecting-artifacts">}}) to Artifactory.

```groovy
artifacts {
    file("myReport", "${project.buildDir}/reports/my_report.json")
}
uploadToArtifactory {
    artifacts = ["myReport"]
}
```

#### Upload to Prosector

{{<avito step>}}

Upload [artifacts]({{< relref "#collecting-artifacts">}}) to [Prosector (internal)](http://links.k.avito.ru/cfxrREPBQ).

```groovy
artifacts {
    apk("debugApk", ...)
}
uploadToProsector {
    artifacts = ["debugApk"]
}
```

#### Upload build results

{{<avito step>}}

Upload all build results to a deploy service.

```groovy
uploadBuildResult {
    uiTestConfiguration = "regression" // instrumentation configuration
}
```

#### Deploy to Google Play

{{<avito step>}}

Deploy to Google play.

```groovy
deploy {}
```

#### Configuration checks

{{<avito check>}}

Checks a repository configuration. See `:build-script-test` for details.

```groovy
    configuration {}
```

### Using impact analysis in step

Сценарий может использовать [Impact analysis]({{< ref "/docs/ci/ImpactAnalysis.md" >}})(по-умолчанию отключено):

```groovy
fastCheck {
    uiTests {
        useImpactAnalysis = true
    }
}
```

### Suppressing errors in step

В разных сценариях падения шагов могут ронять за собой весь билд, а можно настроить чтобы билд не упал.
Обработка этого флага должна быть явно поддержана шагом. 

```groovy
fastCheck {
    uiTests { 
        suppressFailures = false 
    }
}

release {
    uiTests { 
        suppressFailures = true 
    }
}
```

### Collecting artifacts

Артефакты, которые планируется как-то использовать нужно зарегистрировать специальным образом

```groovy
artifacts {
   file("lintReport", "${project.buildDir}/reports/lint-results-release.html")
}
```

Есть разные типы артефактов, различаются по типам проверок и способу описания путей

- apk - достает apk по buildType и проверяют пакет и подпись
- bundle - достает bundle по buildType и проверяют пакет и подпись
- mapping - достает mapping по buildType и проверяют наличие
- file - простой доступ к файлу по пути и проверка на наличие

```groovy
artifacts {
   apk("releaseApk", RELEASE, "com.avito.android", apkPath("release")) { signature = releaseSha1 }
   bundle("releaseBundle", RELEASE, "com.avito.android", bundlePath("release")) { signature = releaseSha1 }
   mapping("releaseMapping", RELEASE, "${project.buildDir}/outputs/mapping/release/mapping.txt")
   file("featureTogglesJson", "${project.buildDir}/reports/feature_toggles.json")
}
```

Первый аргумент - регистрирует ключ, который затем используется в upload шагах для указания артефактов

### Writing a custom step

По необходимости добавляем свои шаги, наследуясь от `BuildStep`
