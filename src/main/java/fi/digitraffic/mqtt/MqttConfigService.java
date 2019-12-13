package fi.digitraffic.mqtt;

import fi.digitraffic.Config;
import fi.digitraffic.hass.ConfigService;
import fi.digitraffic.mqtt.model.MqttConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MqttConfigService {
    private final ConfigService configService;

    private static final Logger LOG = LoggerFactory.getLogger(MqttConfigService.class);

    public MqttConfigService(final ConfigService configService) {
        this.configService = configService;
    }

    public MqttConfig readAndValidate() {
        try {
            final Config config = configService.readConfig(Config.class);
            final MqttConfig mqttConfig = new MqttConfig(config);

            if(validateConfig(mqttConfig)) {
                return mqttConfig;
            }
        } catch(final Exception e) {
            LOG.error("could not get options", e);
        }

        return null;
    }

    private boolean validateConfig(final MqttConfig mqttConfig) {
        boolean notValid = false;

        if(mqttConfig.getOptions().stream().anyMatch(this::isTopicInvalid)) {
            notValid = true;
            LOG.error("wildchars are forbidden!");
        }

        if(mqttConfig.getRoadConfigs().isEmpty() && mqttConfig.getSseConfigs().isEmpty() && mqttConfig.getVesselLocationConfigs().isEmpty()) {
            notValid = true;
            LOG.error("no topics configured!");
        }

        return !notValid;
    }

    private boolean isTopicInvalid(final Config.SensorConfig option) {
        return option.mqttPath.contains("%") || option.mqttPath.contains("*");
    }
}
