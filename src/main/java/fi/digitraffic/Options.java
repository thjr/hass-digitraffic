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

        public SensorOption(final String sensorName, final SensorType sensorType, final String mqttPath, String unitOfMeasurement) {
            this.sensorName = sensorName;
            this.sensorType = sensorType;
            this.mqttPath = String.format("%s/%s", sensorType.toString().toLowerCase(), mqttPath);
            this.unitOfMeasurement = unitOfMeasurement;

            System.out.println("calling constructor!");
        }

        public String getTopic() {
            System.out.println("calling getTopic!");

            return String.format("%s/%s", sensorType.toString().toLowerCase(), mqttPath);
        }
    }

    public enum SensorType {
        WEATHER, TMS, VESSEL
    }
}
