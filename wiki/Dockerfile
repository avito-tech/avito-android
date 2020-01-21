# TODO: MBS-7349
#FROM ruby:2.5.1
# mdl - https://github.com/markdownlint/markdownlint
#RUN gem install mdl -v 0.8.0
#COPY . /docs
#WORKDIR /docs
#RUN mdl /docs/content


# TODO: Use alpine
# Requirements: the latest version of hugo
FROM ubuntu:18.04

RUN apt-get update \
    && apt-get -y install --no-install-recommends \
        curl \
        ca-certificates \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

ARG HUGO_VERSION=0.62.2

RUN curl -LJ -o hugo.deb https://github.com/gohugoio/hugo/releases/download/v${HUGO_VERSION}/hugo_extended_${HUGO_VERSION}_Linux-64bit.deb \
    && apt install ./hugo.deb \
    && rm hugo.deb \
    && hugo version

COPY . /app

WORKDIR /app
