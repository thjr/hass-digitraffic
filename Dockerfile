ARG BUILD_FROM
FROM $BUILD_FROM

COPY target/hass-digitraffic-1.1.1-runner.jar /app.jar

CMD [ "java", "-XX:+UnlockDiagnosticVMOptions", "-XX:SharedArchiveFile=app-cds.jsa", "-jar", "/app.jar" ]

