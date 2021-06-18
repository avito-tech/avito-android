# Remote Cache

???+ warning 
    Currently available only for Avito employees because accessible only via corporate network

???+ warning
    Persistence volume not set for now, deleting a pod will lead to losing cache.

Official docs: https://docs.gradle.com/build-cache-node

## How to enable locally

???+ Prerequisites 
    Ask for <GRADLE_CACHE_NODE_HOST> in internal chat
    
Set environment variable `GRADLE_CACHE_NODE_HOST`

## How to deploy

???+ Prerequisites
    - DOCKER_REGISTRY env set
    - GRADLE_CACHE_NODE_HOST env set
    - [kubernetes credentials configured](http://links.k.avito.ru/kubectl)
    - Write access to `gradle-remote-cache` namespace in cluster

`make deploy_gradle_cache_node`

## Upgrade base image / alter node configuration

See `./ci/docker/gradle-cache-node/`

???+ Prerequisites
    - DOCKER_REGISTRY env set
    - DOCKER_LOGIN env set 
    - DOCKER_PASSWORD env set

After upgrading base image or changing config, new `gradle-cache-node` image must be built and pushed, to do so:

1. call `make internal_publish_gradle_cache_node_image`
1. copy hash from output
1. Makefile: change `GRADLE_CACHE_NODE_TAG` value to copied hash
1. [apply new config](#how-to-deploy)
