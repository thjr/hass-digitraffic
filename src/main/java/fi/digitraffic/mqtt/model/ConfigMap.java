package fi.digitraffic.mqtt.model;

import fi.digitraffic.Config;
import org.springframework.util.StringUtils;

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

        final String[] topicSplit = topic.split("/");

        return topicSplit.length == 3 ? configMap.get(topicSplit[2]) : null;
    }

    public Set<String> keys() {
        return configMap.keySet();
    }
}
