---
title: Android Lint
type: docs
---

# Lint

https://developer.android.com/studio/write/lint

В PR результаты отображаются во вкладках "\<app name\> lint".

## Настройки

Помимо стандартных настроек lint у нас есть:

- Проставляем общие lintOptions для всех модулей в рутовом build.gradle
- Рутовый lint.xml для задания исключений для всех модулей в репозитории
- Плагин для общего lint отчета по всем модулям: `com.avito.android.lint-report`

## Как подавить ошибку?

Используем стандартные возможности lint ([Configure lint to suppress warnings](https://developer.android.com/studio/write/lint.html#config)): 

- Проставить аннотацию @Suppress в коде, если это единичное ложное срабатывание
- Добавить исключение в lint.xml
    - Конкретного модуля (android library).\
    Используем lintOptions.checkDependencies, поэтому может не примениться для приложения, которое подключает модуль. 
    - Приложения (android application)
    - Всего репозитория. Отключит проверку во всех модулях.

## Кастомные проверки

Все кастомные проверки лежат в модуле `lint-checks`.

### Как написать свою проверку?

- KotlinConf 2017 - Kotlin Static Analysis with Android Lint by Tor Norbye: 
[video](https://youtu.be/p8yX5-lPS6o), 
[presentation](https://docs.google.com/presentation/d/1Sr-6E3Tk1lBguUob0GigqSEsfN-04qS56Whj0UEr0AE/edit#slide=id.g29100eff00_2_156)
- [Sample project](https://github.com/googlesamples/android-custom-lint-rules)

Отлично покрываются тестами, но если нужна отладка, то используем общий подход - [debugging]({{< ref "/ci/CustomGradlePlugins.md#debugging" >}})
