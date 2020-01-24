FROM sorccu/adb:latest

RUN apk --no-cache add socat bash

COPY entrypoint.sh /

ENTRYPOINT ["/entrypoint.sh"]
