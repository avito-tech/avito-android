---
title: Android Lint
type: docs
---

# Android Lint

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

## Custom lint checks

All customs android lint checks are in `lint-checks` (internal) module.

### Writing a custom lint check

- [Static Analysis with Android Lint by Tor Norbye (mDevCamp 2019)](https://slideslive.com/38916502) 
- [Sample project](https://github.com/googlesamples/android-custom-lint-rules)
