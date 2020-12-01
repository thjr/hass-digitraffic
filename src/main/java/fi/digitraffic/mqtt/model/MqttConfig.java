package fi.digitraffic.mqtt.model;

import fi.digitraffic.Config;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class MqttConfig {
    private final Map<String, Config.SensorConfig> optionsMap;

    public MqttConfig(final Config config) {
        this.optionsMap = config.sensors.stream().collect(Collectors.toMap(s -> s.mqttPath, s -> s));
    }

    public Collection<Config.SensorConfig> getOptions() {
        return optionsMap.values();
    }

    public ConfigMap getRoadConfigs() {
        return getTopics(Config.SensorType.ROAD);
    }

    public ConfigMap getSseConfigs() {
        return getTopics(Config.SensorType.SSE);
    }

    public ConfigMap getVesselLocationConfigs() {
        return getTopics(Config.SensorType.VESSEL_LOCATION);
    }

    public ConfigMap getTrainGpsConfigs() {
        return getTopics(Config.SensorType.TRAIN_GPS);
    }

    private ConfigMap getTopics(final Config.SensorType sensorType) {
        return new ConfigMap(optionsMap.values().stream()
                .filter(s -> s.sensorType == sensorType)
                .collect(Collectors.toMap(c -> c.mqttPath, c -> c)));
    }

    public boolean noTopics() {
        return optionsMap.isEmpty();
    }
}
