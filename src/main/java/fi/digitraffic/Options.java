package fi.digitraffic;

import java.util.List;

public class Options {
    public final List<SensorOption> sensors;

    public Options(final List<SensorOption> sensors) {
        this.sensors = sensors;
    }

    public static class SensorOption {
        public final String sensorName;
        public final SensorType sensorType;
        public final String mqttPath;
        public final String unitOfMeasurement;

        public SensorOption(final String sensorName, SensorType sensorType, final String mqttPath, String unitOfMeasurement) {
            this.sensorName = sensorName;
            this.sensorType = sensorType;
            this.mqttPath = mqttPath;
            this.unitOfMeasurement = unitOfMeasurement;
        }
    }

    public enum SensorType {
        WEATHER, TMS, VESSEL
    }
}
