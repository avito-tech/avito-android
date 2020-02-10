---
title: Troubleshooting UI Tests
type: docs
---

# Troubleshooting

## Known issues

All known bugs: [jira filter (internal)](http://links.k.avito.ru/Dg)

### Camera auto-focus hangs up on 22 API

[#139438163](https://issuetracker.google.com/issues/139438163)\
There is no workaround. Skip test on this API level.

### Error in local run: "Test framework quit unexpectedly"

Usually it indicates a problem in test runner, see logcat for errors.\
In some cases test can run without problems. Use a [local test report]({{<relref "/ReportViewer.md" >}})

## Как понять почему упал тест?

Посмотри в TeamCity, в тесте краткая выжимка о причинах падения и ссылка на отчет в [Report Viewer]({{<relref "/ReportViewer.md" >}})

## How to deal with flaky test

### 1. Убедись что тест действительно флакует

Посмотри [статистику стабильности теста]({{< ref "#где-посмотреть-статистику-по-стабильности-тестов" >}})

Для проверки запускай тест в несколько прогонов в [динамической конфигурации]({{< ref "/test/DynamicConfig.md" >}}).

### 2. Отлаживай в IDE

В Android Studio должен из коробки работать debug на конкретном тесте.

https://developer.android.com/studio/debug

#### Layout Inspector

<br> *Layout inspector использует adb поэтому мы не можем получить состояние экрана в дебажном запуске.* \
<br> __Как получить экран во время прогона теста?__
1. Добавить в нужное место `Thread.sleep()`.
2. Запустить тест без дебага и дождаться пока исполнение попадет в `Thread.sleep()`
3. Задампить состояние экрана через Layout Inspector

### 3. Если не удалось найти причину

[Обратись за помощью]({{<ref "/test/Support.md" >}})

Обязательно приложи:

- Ссылку на репорт
 
Будет полезно: 

- Логи с ошибками

## Где посмотреть статистику по стабильности тестов?

- [Общая статистика по тестам (internal)](http://links.k.avito.ru/FR)
- [История нестабильности теста (internal)](http://links.k.avito.ru/5W)

Обратите внимание на параметры фильтрации.
