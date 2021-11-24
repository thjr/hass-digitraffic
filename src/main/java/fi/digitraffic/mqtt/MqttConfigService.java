package fi.digitraffic.mqtt;

import fi.digitraffic.Config;
import fi.digitraffic.hass.ConfigService;
import fi.digitraffic.mqtt.model.MqttConfig;
import io.quarkus.logging.Log;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MqttConfigService {
    private final ConfigService configService;

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
            Log.error("could not get options", e);
        }

        return null;
    }

    private boolean validateConfig(final MqttConfig mqttConfig) {
        boolean notValid = false;

        if(mqttConfig.getOptions().stream().anyMatch(this::isTopicInvalid)) {
            notValid = true;
            Log.error("wildchars are forbidden!");
        }

        if(mqttConfig.noTopics()) {
            notValid = true;
            Log.error("no topics configured!");
        }

        return !notValid;
    }

    private boolean isTopicInvalid(final Config.SensorConfig option) {
        return (option.sensorType != Config.SensorType.TRAIN_GPS && option.mqttPath.contains("+")) || option.mqttPath.contains("#");
    }
}
