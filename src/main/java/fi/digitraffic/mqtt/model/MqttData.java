package fi.digitraffic.mqtt.model;

import java.time.ZonedDateTime;

public class MqttData {
    public final String sensorValue;
    public final ZonedDateTime measuredTime;

    public MqttData(final String sensorValue, final ZonedDateTime measuredTime) {
        this.sensorValue = sensorValue;
        this.measuredTime = measuredTime;
    }
}
