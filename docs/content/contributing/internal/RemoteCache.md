# Remote Cache

???+ warning 
    Currently available only for Avito employees via corporate network

[Official docs](https://docs.gradle.com/build-cache-node)

## How to enable for local work

???+ Prerequisites 
    Ask for `GRADLE_CACHE_NODE_HOST` in internal chat
    
Set environment variable `GRADLE_CACHE_NODE_HOST`

## How to deploy

???+ Prerequisites
    - DOCKER_REGISTRY env set
    - GRADLE_CACHE_NODE_HOST env set
    - [kubernetes credentials configured](http://links.k.avito.ru/kubectl)
    - Write access to `gradle-remote-cache` namespace in cluster (see MBS-5444)

???+ warning
    Persistence volume not set, deleting a pod will lead to losing cache.

`make deploy_gradle_cache_node`

## Upgrade base image

1. Look for `GRADLE_CACHE_NODE_TAG` in `/Makefile`. 
1. Change to a new version
1. Publish new version to internal registry via `make internal_publish_gradle_cache_node_image`  
1. [re-deploy](#how-to-deploy) 

## Configuration

[Official docs](https://docs.gradle.com/build-cache-node/#editing_the_file)

Look into `ci/k8s/gradle-remote-cache/`

- To alter node config: `spec.initContainers.command`
- To alter JVM args: `spec.containers.env.JAVA_OPTS`

## Deploy Avito internal node

Same as github. Corresponding command: `make deploy_avito_cache_node`
