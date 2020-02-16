---
title: CI Gradle Plugin
type: docs
---

# CI Gradle plugin

Для настройки CI под конкретный gradle модуль используется in-house плагин.

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
- fastCheck - максимально быстрые проверки, выполняющие [требования к CI]({{< ref "/ci/CIValues.md" >}})
- fullCheck - максимально полные проверки, можем жертвовать скоростью
- release - набор действий необходимых для предоставления всех релизных артефактов

### Steps

Внутри сценариев описываем декларативно необходимые шаги:

```groovy
fastCheck {
    uiTests {}
}
```

- `configuration` - проверить конфигурацию проекта, модуль `build-script-test`
- `uiTests` - запустить ui тесты модуля
- `performanceTests` - запустить перформанс тесты модуля

```groovy
    fastCheck {
        performanceTests {
          configuration = "configuration name"
          enabled = true // true by default
        }
    }
```

enabled - запускать ли шаг. \
configuration - имя performance конфигурации, которая объявленна в instrumentation plugin

- `compileUiTests` - скомпилировать androidTest модуля
- `unitTests` - запустить юнит тесты модуля и всех его зависимостей
- `lint` - запустить android lint по настройкам в модуле
- `docsDeploy` - опубликовать документацию в k8s (отключено)
- `docsCheck` - запустить проверки документации
- `uploadToQapps` - загрузить указанные артефакты в QApps
- `uploadToArtifactory` - загрузить указанные артефакты в Artifactory
- `uploadToProsector` - загрузить указанные артефакты в [Prosector (internal)](http://links.k.avito.ru/cfxrREPBQ)
- `uploadBuildResult` - загрузить указанные артефакты в сервис релизов
- `deploy` - загрузить указанные артефакты в google play
- `artifacts` - зарегистрировать артефакты в качестве результатов билда.\
Добавит проверку на их наличие и возможность загрузить при помощи других тасок.

Сценарий может использовать [Impact analysis]({{< ref "/ci/ImpactAnalysis.md" >}})(по-умолчанию отключено):

```groovy
fastCheck {
    uiTests {
        useImpactAnalysis = true
    }
}
```

#### SuppressibleBuildStep

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

#### Custom steps

По необходимости добавляем свои шаги, наследуясь от `BuildStep`

### Artifacts

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
