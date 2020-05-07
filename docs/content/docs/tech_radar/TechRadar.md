---
title: Technology radar
type: docs
---

# Technology radar

Technology radar is a list of technologies and practices that are described in the same manner:

- An overall status and recommendations for usage
- Limitations
- Reasons for choosing or declining

Original: https://www.thoughtworks.com/radar

## The radar

- [Gradle - buildSrc]({{< relref "buildSrc.md" >}})

[Template]({{< relref "Template.md" >}})

## FAQ

[Detailed FAQ](https://www.thoughtworks.com/radar/faq)

### What questions does tech radar help to solve?

- What criteria guide the choice?
- What should happen to reconsider the choice?
- I want to use X. How can I compare it against Y?

### Do I have to use tech radar for all technologies?

The harder to replace a technology, the more pragmatic we want to be.\
It's needless for something that can be replaced in a week or two.

### Is it mandatory or only recommendations?

Tech radar helps to avoid a [tragedy of the commons](https://en.wikipedia.org/wiki/Tragedy_of_the_commons).\
We all may have different opinions but we are in the same boat.\
Tech radar can't solve any conflict by itself. It only helps to share our knowledge and decisions.

In order to mitigate risks it has different statuses with different requirements.

### Why don't we use Avito's tech radar?

Stand-alone tech radar makes sense when you have dozens of services and repositories with the same common technologies.\
In our case, such tech radar tends to become outdated. It's easier to track changes and keep it up to date together with a product.
