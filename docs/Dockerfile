FROM ruby:2.6.5-alpine3.11 AS linter
# mdl: https://github.com/markdownlint/markdownlint
RUN gem install mdl -v 0.8.0
COPY . /docs
WORKDIR /docs
RUN mdl /docs/content


FROM alpine:3.11

RUN apk update \
    && apk add --update \
    git \
    curl \
    openssh-client \
    ca-certificates \
    # libc libraries are used to overcome https://github.com/gohugoio/hugo/issues/3382 😕
    libc6-compat libstdc++

ARG HUGO_VERSION=0.63.2

RUN curl -L https://github.com/gohugoio/hugo/releases/download/v${HUGO_VERSION}/hugo_extended_${HUGO_VERSION}_Linux-64bit.tar.gz --output /tmp/hugo.tar.gz \
    && tar xzf /tmp/hugo.tar.gz -C /tmp/ \
    && mv /tmp/hugo /usr/local/bin/ \
    && rm -rf /tmp/hugo \
    && rm /var/cache/apk/* \
    && hugo version
