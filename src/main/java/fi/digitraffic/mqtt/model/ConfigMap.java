package fi.digitraffic.mqtt.model;

import fi.digitraffic.Config;

import java.util.Map;
import java.util.Set;

public class ConfigMap {
    // topic to config
    private final Map<String, Config.SensorConfig> configMap;

    public ConfigMap(final Map<String, Config.SensorConfig> configMap) {
        this.configMap = configMap;
    }

    public boolean isEmpty() {
        return configMap.isEmpty();
    }

    public Config.SensorConfig getConfigForTopic(final String topic) {
        final Config.SensorConfig config = configMap.get(topic);

        if(config != null) {
            return config;
        }

        final String matchingKey = configMap.keySet().stream()
                .filter(k -> keyMatches(topic, k))
                .findFirst().orElse(null);

        return matchingKey == null ? null : configMap.get(matchingKey);
    }

    private boolean keyMatches(final String topic, final String key) {
        final String[] topicSplit = topic.split("/");
        final String[] keySplit = key.split("/");

        return topicSplit.length == 3 && keySplit.length == 3 && topicSplit[2].equals(keySplit[2]);
    }

    public Set<String> keys() {
        return configMap.keySet();
    }
}
