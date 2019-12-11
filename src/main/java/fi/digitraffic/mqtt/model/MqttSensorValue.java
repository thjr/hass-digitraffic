package fi.digitraffic.mqtt.model;

import java.time.ZonedDateTime;

public class MqttSensorValue {
    public final String sensorValue;
    public final ZonedDateTime measuredTime;

    public MqttSensorValue(final String sensorValue, final ZonedDateTime measuredTime) {
        this.sensorValue = sensorValue;
        this.measuredTime = measuredTime;
    }
}
