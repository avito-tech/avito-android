---
title: Internals
type: docs
---

# Internals

## Custom click

It is a click without inter-process communication.

### Motivation to write custom click

Espresso click uses inter-process communication. It leads to problems:

- Flaky clicks on moving views. Because click takes some time to touch after coordinates calculating.
- "misinterpret clicks as long clicks"

### Implementation details

Click happens via dispatch touch event on root view.

#### Known issues

- Can click through any system elements on the screen. It applies clicks directly on root
 view of our application. Because of it, crash or permission dialogs can be ignored by
 tests.
- Can click through separate decor view of our application. Sometimes we have multiple
 decor view in application (for example, when we have toolbar overflow menu). And that
 kind of click implementation can click through it.
- Overlapped view can't handle click. We can't realize when view is overlapped. Because of that clicks on overlapped views don't work and don't throw errors.

##### Overlapped view click details

We tried to validate that click happens. \
We were able to check that clicked view wasn't overlapped, but faced scenario when user clicked on container and expected it to delegate click handling to child.
We don't want to restrict our user, because many tests behave like that.

