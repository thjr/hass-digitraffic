package fi.digitraffic;

import java.util.List;

public class Config {
    public List<SensorConfig> sensors;

    public static class SensorConfig {
        public String sensorName;
        public SensorType sensorType;
        public String mqttPath;
        public String unitOfMeasurement;
    }

    public enum SensorType {
        ROAD, VESSEL, SSE
    }
}
