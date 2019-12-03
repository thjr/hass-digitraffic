package fi.digitraffic;

import java.util.List;

public class Options {
    public final List<SensorOption> sensorOptions;

    public Options(List<SensorOption> sensorOptions) {
        this.sensorOptions = sensorOptions;
    }

    public static class SensorOption {
        public final String sensorName;
        public final String mqttPath;
        public final String unitOfMeasurement;

        public SensorOption(final String sensorName, final String mqttPath, String unitOfMeasurement) {
            this.sensorName = sensorName;
            this.mqttPath = mqttPath;
            this.unitOfMeasurement = unitOfMeasurement;
        }
    }
}
