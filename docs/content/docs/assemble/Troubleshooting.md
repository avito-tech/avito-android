---
title: Troubleshooting
type: docs
---

# Troubleshooting

{{<avito page>}}

## Что делать в случае непонятной ошибки?

Ситуация: падает или подвисает синхронизация проекта или сборка. По ошибке ничего не понятно.

Чтобы найти причину, проще всего последовательно исключать возможные причины.

- Исключи влияние локальных изменений: проверь на свежем develop
- Проверь что включен VPN
- Исключи влияние mirakle: `./mirakle.py -d`
- Исключи влияние IDE: проверь сборку из консоли
- Проверь не переопределено ли что-то подозрительное в `~/.gradle/gradle.properties`
- Убедись что конфигурация проекта проходит успешно: `./gradlew help`
- Посмотри детальную ошибку: `./gradlew <failed task> --stacktrace`
- Исключи влияние кеширования: `./gradlew <failed task> --no-configuration-cache --no-build-cache`\
Очистить кеш можно командой: `./gradlew cleanBuildCache`

### Если проблема воспроизводится только в IDE

В консоли отработало без ошибок, но в IDE падает или подвисает.

- Проверь версию IDE и Kotlin плагина. 
Возможно они слишком старые или наоборот, alpha/beta версии.
- Добавь `--stacktrace` чтобы увидеть детали ошибки:\
**Settings > Build, Execution, Deployment > Compiler > Command-line Options:**_
- Проверь что не включен offline mode на вкладке Gradle
- Возможно ошибка в .iml, .idea/ файлах:
    - `./clean.py --all` или **File > Re-Import Gradle project**
    - **File > Invalidate Caches / Restart**
- Отключи все прокси (Charles и т.п.). Они могут перехватывать по ошибке лишние запросы из IDE и сборки.
- Посмотри логи **Help > Show log in Finder**

### Если причина в Mirakle

Возможно из mirakle прилетают некорректные данные. Удали их: `./clean.py -r`

### Если ничего не помогло

- Отправь build scan:\
`./gradlew <task> --scan` или `./gradlew buildScanPublishPrevious`;
- Напиши в #android-dev или #speed, приложи ссылку на build scan и все что удалось проверить.

## Как искать проблемы с кешированием?

[Gradle - Using build cache](https://guides.gradle.org/using-build-cache/)

## Known issues

### D8: Dex file with version 'N' cannot be used with min sdk level 'M'

```none
Dex file with version '38' cannot be used with min sdk level '22'. D8
    com.android.builder.dexing.DexArchiveMergerException: Error while merging dex archives
``` 

Предположительно возникает после изменений в плагинах.\
Помогает `./clean.py -a`
