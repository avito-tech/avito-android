---
title: Report Viewer
type: docs
---

# Report Viewer

{{<avito page>}}

Используем отчеты в [Report Viewer (internal)](http://links.k.avito.ru/cfxRp7KAg), которые содержат:

- **Видео** с эмулятора
    - Только для упавших тестов
    - Только для API 23+. На меньших версиях технологии не позволяют записывать надежно
- **Скриншоты:** до и после каждого шага, во время падения
- **Трейс ошибки**: цепочка событий которая привела к ошибке
- **Logcat** для упавших тестов
- **HTTP** запросы и ответы во время шагов
- **Логи запросов к ресурсам**: Resource Manager, AB/test, Integration API, phones
- **Логи действий тестового фреймворка**

## Using report viewer in a local run

Report Viewer works in local runs too.\
Search a link in a logcat by `rv.k`.
