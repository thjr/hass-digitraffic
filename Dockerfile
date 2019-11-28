ARG BUILD_FROM
FROM $BUILD_FROM

COPY target/hass-digitraffic-1.0.jar /app.jar

# install java
RUN \
    apk add --no-cache \
        openjdk8-jre=8.222.10-r0 \
    && rm -fr /tmp/*

CMD [ "java", "-jar", "/app.jar" ]

