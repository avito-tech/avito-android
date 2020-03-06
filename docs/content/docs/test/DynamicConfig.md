---
title: Dynamic config
type: docs
---

# Запуск тестов в CI с кастомными параметрами (internal)

В Teamcity есть [конфигурация instrumentationDynamic](http://links.k.avito.ru/tmctAvitoAndroidInstrumentationDynamic) 
для запуска тестов со специфическим набором требований.

Запускайте и вам будет предложено выбрать:

- Ветку для запуска
- Версии API
- Фильтр для выбора тестов (package prefix), можно задать пакет [+ имя класса [+ имя тестового метода ]] 
- Количество запусков. Полезно для [отладки нестабильных тестов]({{< ref "/docs/test/TroubleshootingUI.md" >}})
