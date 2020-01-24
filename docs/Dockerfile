FROM ruby:2.6.5-alpine3.11
# mdl: https://github.com/markdownlint/markdownlint
RUN gem install mdl -v 0.8.0
COPY . /docs
WORKDIR /docs
RUN mdl /docs/content


FROM alpine:3.11

# libc libraries are used to overcome https://github.com/gohugoio/hugo/issues/3382 😕
RUN apk update \
    && apk add --update libc6-compat libstdc++

ARG HUGO_VERSION=0.63.1

RUN mkdir -p /tmp/hugo
ADD https://github.com/gohugoio/hugo/releases/download/v${HUGO_VERSION}/hugo_extended_${HUGO_VERSION}_Linux-64bit.tar.gz /tmp/hugo
RUN tar xzf /tmp/hugo/*.tar.gz -C /tmp/hugo/ \
    && mv /tmp/hugo/hugo /usr/local/bin/ \
    && rm -rf /tmp/hugo \
    && rm /var/cache/apk/* \
    && hugo version
