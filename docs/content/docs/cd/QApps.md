---
title: QApps
type: docs
---

# QApps

{{<avito page>}}

[QApps Android](http://links.k.avito.ru/qappsAndroid)

This is the internal storage with bulds for manual testing.\
We publish builds automatically after merge in develop.

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

{{< tab "CI" >}}

Run [uploadArtifacts](http://links.k.avito.ru/Mx9) Teamcity configuration.\
Choose your custom branch if needed.

{{< /tab >}}
{{< /tabs >}}

## How to change upload settings?

In `build.gradle` you can find [uploadToQapps]({{< ref "/docs/ci/CIGradlePlugin.md#upload-to-qapps" >}}) build step. It defines what to publish.\
We run this step in [uploadArtifacts](http://links.k.avito.ru/Mx9) Teamcity configuration.

