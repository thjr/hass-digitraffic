package fi.digitraffic.mqtt;

import fi.digitraffic.Options;
import fi.digitraffic.hass.OptionsService;
import fi.digitraffic.mqtt.model.MqttOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
public class MqttOptionService {
    private final OptionsService optionsService;

    private static final Logger LOG = LoggerFactory.getLogger(MqttOptionService.class);

    public MqttOptionService(OptionsService optionsService) {
        this.optionsService = optionsService;
    }

    public MqttOptions readAndValidate() {
        try {
            final Options options = optionsService.readOptions(Options.class);
            final Map<String, Options.SensorOption> optionsMap = options.sensors.stream().collect(Collectors.toMap(s -> s.mqttPath, s -> s));

            if(validateOptions(optionsMap)) {
                return new MqttOptions(optionsMap);
            }
        } catch(final Exception e) {
            LOG.error("could not get options", e);
        }

        return null;
    }

    private boolean validateOptions(final Map<String, Options.SensorOption> options) {
        boolean valid = true;

        if(options.values().stream().anyMatch(this::isTopicValid)) {
            LOG.error("wildchars are forbidden!");
            valid = false;
        }

        return valid;
    }

    private boolean isTopicValid(final Options.SensorOption option) {
        return !(option.mqttPath.contains("%") || option.mqttPath.contains("*"));
    }
}
