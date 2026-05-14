ARG UPSTREAM_REF=v0.2.1

FROM golang:1.24 AS build
ARG UPSTREAM_REF
WORKDIR /src
RUN git clone --depth 1 --branch ${UPSTREAM_REF} \
    https://github.com/salesforce/kubelet-summary-exporter.git . \
 && CGO_ENABLED=0 GOOS=linux go build -trimpath -ldflags="-s -w" \
    -o /out/kubelet-summary-exporter ./cmd/kubelet-summary-exporter

FROM gcr.io/distroless/static-debian12:nonroot
COPY --from=build /out/kubelet-summary-exporter /kubelet-summary-exporter
USER 65532:65532
ENTRYPOINT ["/kubelet-summary-exporter"]
