package fi.digitraffic.hass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class SensorValueService {
    private static final String HASS_ADDRESS = "hassio";
    private final String hassToken;

    private static final Logger LOG = LoggerFactory.getLogger(SensorValueService.class);

    public SensorValueService(@Value("{HASS_TOKEN}") final String hassToken) {
        this.hassToken = hassToken;
    }

    public void postSensorValue(final String entity, final String sensor, final String value) {
        final String url = String.format("%s/api/states/sensor.%s_%s", HASS_ADDRESS, entity, sensor);
        final HassStateData data = new HassStateData(value, Collections.emptyList());
    }

    private static class HassStateData {
        public final Object state;
        public final List<String> attributes;

        private HassStateData(Object state, List<String> attributes) {
            this.state = state;
            this.attributes = attributes;
        }
    }
}
