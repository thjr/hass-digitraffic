ARG BUILD_FROM
FROM $BUILD_FROM

COPY target/hass-digitraffic-1.1.jar /app.jar

# install java
RUN \
    apk add --no-cache \
        openjdk8-jre \
    && rm -fr /tmp/*

CMD [ "/usr/bin/java", "-XX:SharedArchiveFile=app-cds.jsa", "-jar", "/app.jar" ]

