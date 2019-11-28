package fi.digitraffic.mqtt.model;

import java.time.ZonedDateTime;

public class WeatherData {
    public final String sensorValue;
    public final ZonedDateTime measuredTime;

    public WeatherData(final String sensorValue, final ZonedDateTime measuredTime) {
        this.sensorValue = sensorValue;
        this.measuredTime = measuredTime;
    }
}
