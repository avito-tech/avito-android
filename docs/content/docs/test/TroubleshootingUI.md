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

[Работа с флакующими тестами]({{< ref "docs/development/DealingWithFlaky.md" >}})
