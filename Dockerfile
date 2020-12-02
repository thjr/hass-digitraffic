ARG BUILD_FROM
FROM $BUILD_FROM

COPY target/quarkus-app/quarkus-run.jar /app.jar

# install java
RUN \
    apk add --no-cache \
        openjdk8-jre \
    && rm -fr /tmp/*

CMD [ "/usr/bin/java", "-XX:+UnlockDiagnosticVMOptions", "-XX:SharedArchiveFile=app-cds.jsa", "-jar", "/app.jar" ]

