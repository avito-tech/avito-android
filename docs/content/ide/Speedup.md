---
title: IDE Speedup
type: docs
---

# Ускорение IDE

Чтобы IDE меньше тормозила при работе с проектом, попробуй эти настройки:


## Выдай IDE больше памяти  

По умолчанию IDE выделяет мало, чтобы суметь запуститься на слабой машине.

_Help | Edit custom VM Options_

```none
-Xmx4g
```

Слишком много не нужно. Чтобы посмотреть реальное потребление памяти:

_Appearance & Behavior | Appearance | Show memory indicator_


## Включи удаленную сборку 

[Mirakle]({{< ref "/assemble/Mirakle.md" >}})


## Оставь только необходимые приложения

Подключаем в settings.gradle только часть приложений, чтобы ускорить работу с проектом. 
Посмотри в gradle.properties флаги `sync...` и выбери какие нужны.   
Переопределяй в системных настройках (`~/.gradle/gradle.properties`):

```ini
syncAvito=false
sync...
```

После изменения синхронизируй проект.


## Отключи излишние действия во время синхронизации

- _Preferences | Experimental | Skip source generation on Gradle sync_
- _Preferences | Experimental | Skip download of sources and javadoc on Gradle sync_
- _Preferences | Experimental | Gradle | Only sync the active variant_


## Отключи автоматическую синхронизацию проекта

_Preferences | Build, Execution, Deployment | Compiler | Sync project with Gradle before building_


## Проверь что Gradle и Kotlin не запускают несколько демонов

```bash
./gradlew --status
```

Если запущено несколько: https://stackoverflow.com/c/avito/questions/109


## Отключи неиспользуемые плагины

- Android APK Support
- Android Games
- Android NDK
- App Links
- Assistant
- CVS, hg4idea, Subversion integration
- Firebase <product>
- GitHub
- Google <product>
- Task management
- Terminal
- Test recorder, TestNG
- YAML


## Отключи индексацию директорий (Spotlight, Антивирус)

При сборке проект генерирует много файлов, что вызывает постоянную переиндексацию.

OSX

Добавь в исключения Spotlight: _System preferences | Spotlight | Privacy_

- Директории с android проектами (обязательно)
- Android SDK
- Android Studio
- ~/.gradle
- ~/.android
- ~/.gradle-profiler
- ~/gradle-profiler
- ~/.m2
- ~/Android StudioX.X ?
- ~/lldb ?

tip: показать скрытые директории в Finder: _Cmd + Shift + ._

## Скрой ненужные директории и типы файлов

Проект большой и в нем много сгенерированных файлов, что нагружает IDE.
Чтобы немного помочь, скрой файлы, которые никогда не нужны:
 
_Preferences | Editor | File Types | Ignore Files and Folders_

Недостатки:

Про эти исключения легко забыть, неочевидно, может помешать:

- *build* - не будут видны BuildConfig файлы, сгенерированные файлы, вообще ничего.
- *.gradle;intermediates;kotlin-classes;caches-jvm;* - intermediates файлы сборки, обычно не нужны.
- *apt;kapt;kaptKotlin;* не будут видны Dagger файлы.   
Они будут красными в редакторе и автоформатирование может удалять их импорты.


## kotlin.use.ultra.light.classes (experimental)

A light class is a representation of a Kotlin class as the Java PSI, allowing IntelliJ IDEA's Java support features to work with Kotlin classes.

`Cmd + Shift + A` - найди Registry, включи флаг `kotlin.use.ultra.light.classes`.


## Освободи в ОС больше памяти

Посмотри какие приложения потребляют много памяти, не используется ли swap.

Для Google Chrome есть плагины для авто-остановки старых вкладок: [The Great suspender](https://chrome.google.com/webstore/detail/the-great-suspender/klbibkeccnjlkjkiokjodocebajanakg?hl=en)

![](https://lh3.googleusercontent.com/hVzJ8OibWTB1JRqu0_2w3gY_nfPKiVfOLAwC93OtHWvWrSKOk-QgCF3rvLQgFhXUb79Aidq3OQ=w640-h400-e365)


## Включи режим энергосбережения

Отключает инспекции и подсветку синтаксиса

_File | Power Save Mode_
