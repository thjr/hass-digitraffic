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

        return post(url, data);
    }

    public int postLocation(final String entityName, final String latitude, final String longitude, String navStat, String heading, String sog) throws IOException {
        final URL url = new URL(String.format("http://%s/api/states/sensor.%s", HASSIO_ADDRESS, entityName));
        final Map<String, String> attributes = new HashMap<>();
        final HassStateData data = new HassStateData(navStat, attributes);
        attributes.put("latitude", latitude);
        attributes.put("longitude", longitude);
        attributes.put("heading", heading);
        attributes.put("sog", sog);

        return post(url, data);
    }

    public int postTrainGps(final String entityName, final String latitude, final String longitude, final String speed) throws IOException {
        final URL url = new URL(String.format("http://%s/api/states/sensor.%s", HASSIO_ADDRESS, entityName));
        final Map<String, String> attributes = new HashMap<>();
        final HassStateData data = new HassStateData("OK", attributes);
        attributes.put("latitude", latitude);
        attributes.put("longitude", longitude);
        attributes.put("speed", speed);

        return post(url, data);
    }

    private int post(final URL url, final HassStateData data) throws IOException {
        final String message = gson.toJson(data);
        final HttpURLConnection con = (HttpURLConnection) url.openConnection();

        LOG.info("posting to {}", url.getPath());

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
