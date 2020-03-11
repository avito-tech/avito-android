---
title: Test Management System
type: docs
---

# Test Management System (internal)

Avito uses in-house TMS: [Internal docs](http://links.k.avito.ru/h)

## Metadata

`@FeatureId(IntArray)` - specify test's place in global features tree
`@TagId(IntArray)` - specify id's of team tag cloud

-- deprecated --

`@CaseId(Int)` - TMS id, please consider [test case in code]({{< ref "/docs/test/TestCaseInCode.md#test-case-in-code" >}})

## Kind

Kind is specified to map test on test pyramid in TMS, see `com.avito.report.model.Kind`

These annotations also used to filter tests for different suites.

- @E2ETest - e2e functional tests
- @UIComponentTest - UI tests without(or minimal) e2e networking
- @IntegrationTest - Instrumentation tests without UI
- [@ManualTest]({{< ref "/docs/test/TestCaseInCode.md#stubs-tests-without-implementation" >}})
- [@UIComponentStub]({{< ref "/docs/test/TestCaseInCode.md#stubs-tests-without-implementation" >}})
- [@E2EStub]({{< ref "/docs/test/TestCaseInCode.md#stubs-tests-without-implementation" >}})
- @UnitTest - Classical unit tests that should be synced with TMS
- [@PerformanceFunctionalTest]({{< ref "/docs/test/PerformanceTesting.md" >}})
- [@PerformanceComponentTest]({{< ref "/docs/test/PerformanceTesting.md" >}})
- [@ScreenshotTest]({{< ref "/docs/test/ScreenshotTesting.md" >}})

-- deprecated --

- @ComponentTest -> @UIComponentTest
- @PublishTest -> @UIComponentTest
- @MessengerTest -> @UIComponentTest
- @FunctionalTest -> @E2ETest
- @InstrumentationUnitTest -> @IntegrationTest
