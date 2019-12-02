package fi.digitraffic.hass;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.List;

@Component
public class SensorValueService {
    private static final String HASS_ADDRESS = "hassio/homeassistant";
    private static final Logger LOG = LoggerFactory.getLogger(SensorValueService.class);

    private final String hassToken;
    private final Gson gson = new Gson();

    public SensorValueService(@Value("{HASS_TOKEN}") final String hassToken) {
        this.hassToken = hassToken;
    }

    public int postSensorValue(final String sensorName, final String value) throws IOException {
        final URL url = new URL(String.format("http://%s/api/states/sensor.%s", HASS_ADDRESS, sensorName));
        final HassStateData data = new HassStateData(value, Collections.emptyList());
        final HttpURLConnection con = (HttpURLConnection) url.openConnection();
        final String message = gson.toJson(data);

        LOG.info("posting {} to {}", message, url.getPath());

        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("X-HA-Access", hassToken);
        con.setRequestMethod("PUT");
        con.setDoOutput(true);
        con.getOutputStream().write(message.getBytes());

        return con.getResponseCode();
    }

    private static class HassStateData {
        final Object state;
        final List<String> attributes;

        private HassStateData(Object state, List<String> attributes) {
            this.state = state;
            this.attributes = attributes;
        }
    }
}
