package fi.digitraffic.mqtt.model;

import fi.digitraffic.Config;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MqttConfig {
    final Map<String, Config.SensorConfig> optionsMap;
    final List<String> roadTopics;

    public MqttConfig(final Config config) {
        this.optionsMap = config.sensors.stream().collect(Collectors.toMap(s -> s.mqttPath, s -> s));
        this.roadTopics = config.sensors.stream().filter(s -> s.sensorType == Config.SensorType.ROAD).map(s -> s.mqttPath).collect(Collectors.toList());
    }

    public Config.SensorConfig getOption(final String topic) {
        return optionsMap.get(topic);
    }

    public Collection<Config.SensorConfig> getOptions() {
        return optionsMap.values();
    }

    public List<String> getRoadTopics() {
        return roadTopics;
    }
}
