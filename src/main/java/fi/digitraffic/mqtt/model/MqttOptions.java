package fi.digitraffic.mqtt.model;

import fi.digitraffic.Options;

import java.util.Collection;
import java.util.Map;

public class MqttOptions {
    final Map<String, Options.SensorOption> optionsMap;

    public MqttOptions(Map<String, Options.SensorOption> optionsMap) {
        this.optionsMap = optionsMap;
    }

    public Options.SensorOption getOption(final String topic) {
        return optionsMap.get(topic);
    }

    public Collection<Options.SensorOption> getOptions() {
        return optionsMap.values();
    }
}
