package fi.digitraffic.hass;

import com.google.gson.Gson;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static java.net.HttpURLConnection.HTTP_OK;

@ApplicationScoped
public class SensorValueService {
    private static final String HASSIO_ADDRESS = "hassio/homeassistant";
    private static final Logger LOG = LoggerFactory.getLogger(SensorValueService.class);

    private final boolean skipWrite;
    private final String hassioToken;
    private final Gson gson = new Gson();
    private final ResteasyClient client;

    public SensorValueService(@ConfigProperty(name = "digitraffic.hass.token") final String token, @ConfigProperty(name = "digitraffic.skip_write") final boolean skipWrite) {
        this.skipWrite = skipWrite;
        this.hassioToken = token;
        this.client = new ResteasyClientBuilderImpl().build();
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

    private synchronized int post(final URL url, final HassStateData data) {
        LOG.debug("posting to {}", url.getPath());

        if(skipWrite) {
            return 200;
        }
        try {
            final Response response = client.target(url.toURI()).request()
                    .header("Content-Type", "application/json")
                    .header("X-HA-Access", hassioToken)
                    .post(Entity.text(gson.toJson(data)));

            final int httpCode = response.getStatus();

            response.close();

            if(httpCode != HTTP_OK) {
                LOG.error("Posting to {} returned {}", url.getPath(), httpCode);
            }

            return httpCode;
        } catch(final Exception e) {
            LOG.error("Exception posting to " + url.getPath(), e);

            return -1;
        }
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
