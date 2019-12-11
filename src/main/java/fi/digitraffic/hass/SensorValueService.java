package fi.digitraffic.hass;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensorValueService {
    private static final String HASSIO_ADDRESS = "hassio/homeassistant";
    private static final Logger LOG = LoggerFactory.getLogger(SensorValueService.class);

    private final String hassioToken;
    private final Gson gson = new Gson();

    public SensorValueService(@Value("${HASSIO_TOKEN}") final String token) {
        this.hassioToken = token;
    }

    public int postSensorValue(final String sensorName, final String value, final String unitOfMeasurement) throws IOException {
        final URL url = new URL(String.format("http://%s/api/states/sensor.%s", HASSIO_ADDRESS, sensorName));
        final Map<String, String> attributes = new HashMap<>();
        final HassStateData data = new HassStateData(value, attributes);
        attributes.put("unit_of_measurement", unitOfMeasurement);

        final HttpURLConnection con = (HttpURLConnection) url.openConnection();
        final String message = gson.toJson(data);

        LOG.info("posting {} to {}", message, url.getPath());

        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("X-HA-Access", hassioToken);
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        con.getOutputStream().write(message.getBytes());
        con.connect();

        return con.getResponseCode();
    }

    private static class HassStateData {
        final Object state;
        final Map<String, String> attributes;

        private HassStateData(final Object state, final Map<String, String> attributes) {
            this.state = state;
            this.attributes = attributes;
        }
    }
}
