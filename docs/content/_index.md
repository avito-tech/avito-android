---
title: Introduction
type: docs
---

# Android in Avito

Multiple projects live in two repositories:

- Open Source repository ([avito-tech/avito-android](https://github.com/avito-tech/avito-android)) - monorepo of all tooling to continuously test and deliver apps to users
- Internal repository (closed source) - monorepo of all apps and shared libraries

## Open Source repository

Contacts: Telegram chat - [English](https://t.me/avito_android_opensource_en), [Russian](https://t.me/avito_android_opensource)

All source code lives in `subprojects/`:

- `android-test` - code that goes in androidTestImplementation configuration and runs on emulators
- `gradle` - Gradle plugins and buildscript dependencies
- `common` - shared code between `android-test` and `gradle`

More details - [project structure]({{< ref "/Infrastructure.md" >}})

Besides these modules you will find:

- `ci/` and `/*.sh` - we follow [IaC](https://en.wikipedia.org/wiki/Infrastructure_as_code) principle whenever possible. 
You can see docker images we use to abstract configuration of apps building and testing, as well as testing github project itself.
- `docs/` - documentation you see right now and code to deploy it automatically.\
[How we document]({{< ref "/docs/contributing/Docs.md" >}})
{{< hint warning>}}
You could see links to "internal" resources that are not available for non-employees (links.k.avito.ru).\
It has been done on purpose to have single documentation and show the whole picture.
{{< /hint >}}

## Closed source internal repository

- [How to start]({{< ref "/docs/contributing/HowToStart.md" >}})
