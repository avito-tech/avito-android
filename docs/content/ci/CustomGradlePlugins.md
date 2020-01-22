---
title: Custom Gradle Plugins
type: docs
---

# Custom Gradle plugins

Вся логика CI расположена в in-house gradle плагинах. 
Для тестирования корневого проекта смотри модуль `build-script-test`.

## How to start

Начни с официальных туториалов, они сэкономят время:

- [Gradle plugin development tutorials](https://gradle.org/guides/?q=Plugin%20Development)   
Для нас не актуальна только публикация плагинов.
- [Custom tasks](https://docs.gradle.org/current/userguide/custom_tasks.html)

Если что-то не понятно, здесь тебе помогут:

- [#gradle](http://links.k.avito.ru/slackgradle)
- [gradle-community.slack.com](gradle-community.slack.com)

## Работа с плагинами в IDE

1. Предпочтительно использовать IntelliJ IDEA
1. Import project
1. Согласись использовать gradle wrapper
1. Settings > Build, Execution, Deployment > Build Tools > Gradle > Runner
    1. Delegate IDE build/run actions to gradle (check)
    1. Run tests using : Gradle Test Runner
    
Теперь можно запускать тесты по иконкам run

Known issues:

- Имя теста (DynamicTest.displayName) некорректно отображается в IDE: [#5975](https://github.com/gradle/gradle/issues/5975)

## Debugging

Для тестов работает из IDE.   
Для отладки плагина:

- Добавь в IDE конфигурацию Remote для запуска, как для обычного java проекта.
- Запускай gradle из корня репозитория с параметрами `-Dorg.gradle.debug=true --no-daemon`

Debugger работает не только в нашем коде, остановиться можно и в AGP или Gradle.

## Тестирование

### Запуск тестов из консоли

`./gradlew test`

Чтобы не останавливать прогон тестов на первом падении добавь `--continue`

Для запуска отдельного теста, класса или пакета работает фильтр: `--tests package.class.method`, 
но нужно запускать тесты для отдельного модуля, иначе фильтр упадет не найдя нужных тестов по фильтру 
в первом попавшемся модуле.

## Best practices

### Feature toggles

Плагин может сломаться и заблокировать всем работу с проектом. 
Чтобы дать себе время на исправление, делай плагин отключаемым:

```kotlin
open class MyPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // В каждом property используем префикс `avito.<plugin>`
        // сразу видно где используется
        if (!project.getBooleanProperty("avito.my_plugin.enabled", default = false)) {
            project.logger.lifecycle("My plugin is disabled")
            return
        }
```

Тогда каждый разработчик сможет локально отключить плагин в случае проблем. 

## Директория ci

Там храним всю интеграцию с CI.   
Часто нужно править плагин совместно с ./ci/

Чтобы работать одновременно со всем этим кодом, к уже открытому проекту
добавь модуль ci: File > New > Module from existing sources > путь до папки ci > ok > ok

## Интеграция плагина в CI

[CI Gradle Plugin]({{< ref "/ci/CIGradlePlugin.md" >}})

## Дополнительные материалы

- [Интеграция с AGP 4+](https://youtu.be/OTANozHzgPc)
