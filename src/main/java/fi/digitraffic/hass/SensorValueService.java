package fi.digitraffic.hass;

import fi.digitraffic.mqtt.MqttService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SensorValueService {
    private final String hassAddress;
    private final String hassToken;

    private static final Logger LOG = LoggerFactory.getLogger(SensorValueService.class);

    public SensorValueService(@Value("${HASS_HOST}") final String hassAddress, @Value("{HASS_TOKEN}") final String hassToken) {
        this.hassAddress = hassAddress;
        this.hassToken = hassToken;

        LOG.info("address={} token={}", hassAddress, hassToken);
    }
}
