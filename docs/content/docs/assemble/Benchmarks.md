---
title: Бенчмарки сборки
type: docs
---

# Бенчмарки сборки

{{<avito page>}}

Для бенчмарков используем [Gradle profiler](https://github.com/gradle/gradle-profiler), 
где можно описать воспрозводимый сценарий:

- Редактировать java/kotlin файлы, Android ресурсы
- Переключаться между коммитами
- Проверить на разных версиях Gradle
- Почистить кеш, сборку
- Учесть прогрев перед измерениями

## How to start

- Установи вручную или скриптом: `benchmarks/profiler.py --install`
- Опиши свой сценарий. 
Возьми за основу один из готовых из `ci/profiler/benchmarks.scenarios` и посмотри какие [операции](https://github.com/gradle/gradle-profiler#advanced-profiling-scenarios) необходимы.

Меняем код и ресурсы в модуле serp-core и собираем avito:

```typescript
serp_avito_assemble {
    tasks = [":avito:assembleDebug"]
    apply-abi-change-to = "avito-libs/serp-core/src/main/java/com/avito/android/serp/SerpResult.kt"
    apply-android-resource-change-to = "avito-libs/serp-core/src/main/res/values/strings.xml"
} 
```

Сохраняем сценарий в файл.

- Запусти профайлер:

```bash
gradle-profiler --benchmark \
    --project-dir . \
    --warmups 2 \
    --iterations 5 \
    --gradle-user-home ~/.gradle-profiler \ 
    --output-dir benchmarks/output \
    --scenario-file scenarios \
    serp_avito_assemble
```

Все [параметры запуска](https://github.com/gradle/gradle-profiler#command-line-options)

## Что измеряем в CI?

Измеряем сценарии из `ci/profiler/benchmarks.scenarios`. 

Запускаем в develop в конфигурации [Profiler build (internal)](http://links.k.avito.ru/tmctAvitoAndroidProfiler). 
Собираем только пару раз в день, потому что на каждый коммит не хватает железа. 

[Dashboard (internal)](http://links.k.avito.ru/Fa) с этими сценариями.
