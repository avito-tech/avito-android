---
title: QApps
type: docs
---

# QApps

{{<avito page>}}

[QApps Android](http://links.k.avito.ru/qappsAndroid)

Внутреннее хранилище сборок для ручного тестирования.\
Заливаем в него apk по расписанию из develop.\
См. конфиг `uploadToQapps` в `build.gradle` приложений.

## Как залить вручную в QApps с ветки?

{{< tabs "qapps" >}}
{{< tab "Локально" >}}

Для каждого приложения есть задача `qappsUpload<build variant>`.\
Она заливает уже собранную apk.

1. Собери apk
1. `./gradlew :avito:qappsUploadStaging -Pci=true`\
Чтобы было проще найти сборку, укажи комментарий аргументом `-PbuildNumber="my custom build"`

## Known issues

- Для релизной сборки не совсем подходит, т.к. не будет подписана релизным сертификатом.
- Чтобы отправить с другими versionName, versionCode нужно собрать приложение с этими параметрами.\
Для qapps это только комментарии к бинарнику. 
Даже если обмануть и залить apk в qapps указав другую версию, содержимое apk от этого не изменится.

{{< /tab >}}

{{< tab "Из CI" >}}
Запусти конфигурацию [fullCheck](http://links.k.avito.ru/tmctAvitoAndroidFullCheck)\
Выбери кастомный запуск, укажи свою ветку.

## Known issues

**Длится до 1 часа**, запускает все тесты.\
Чтобы ускорить, закомментируй лишние [шаги]({{< ref "/docs/ci/CIGradlePlugin.md#steps" >}}) в `fullCheck` конфиге в `build.gradle` приложений.\
Задача на более быстрый способ: [MBS-7340](http://links.k.avito.ru/MBS7340)
{{< /tab >}}
{{< /tabs >}}



