package fi.digitraffic;

import java.util.List;

public class Options {
    public List<SensorOption> sensors;

    public static class SensorOption {
        public String sensorName;
        public SensorType sensorType;
        public String mqttPath;
        public String unitOfMeasurement;
    }

    public enum SensorType {
        ROAD, VESSEL, SSE
    }
}
