# Docker images

--8<--
avito-disclaimer.md
--8<--

[Avito Docker documentation (internal)](http://links.k.avito.ru/cfxOMToAQ)

Docker images located at `ci/docker`

We have:

- [Image builder](ImageBuilder.md)
- [Android builder image](AndroidBuilderImage.md)
- [Android emulator image](AndroidEmulatorImage.md)

## Best practices

### Reproducible image

We want to build equivalent images on any host on any environment. It helps debug and troubleshooting plus make image more reliable.

[reproducible-builds.org](https://reproducible-builds.org/docs/definition/)

Sources of the instability:

- Copy generated out of Docker files to images. It's hard to say where the were created and what content is expected
