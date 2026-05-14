ARG DOCKER_REGISTRY
ARG ARTIFACTORY_URL
ARG UPSTREAM_REF=v0.2.1

FROM ${DOCKER_REGISTRY}/avito/golang:1.24 AS build
ARG ARTIFACTORY_URL
ARG UPSTREAM_REF
ENV GOFLAGS=-mod=vendor CGO_ENABLED=0 GOOS=linux
WORKDIR /src

RUN curl -fsSL "${ARTIFACTORY_URL}android-build-env/exporters/kubelet-summary-exporter-${UPSTREAM_REF}-vendored.tar.gz" \
        -o /tmp/src.tar.gz \
 && tar -xzf /tmp/src.tar.gz --strip-components=1 \
 && rm /tmp/src.tar.gz \
 && go build -trimpath -ldflags="-s -w" \
    -o /out/kubelet-summary-exporter ./cmd/kubelet-summary-exporter

FROM ${DOCKER_REGISTRY}/avito/service-static-non-root:2.0.0
COPY --from=build /out/kubelet-summary-exporter /kubelet-summary-exporter
ENTRYPOINT ["/kubelet-summary-exporter"]
