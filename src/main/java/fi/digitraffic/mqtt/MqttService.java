package fi.digitraffic.mqtt;

import com.google.gson.*;
import fi.digitraffic.mqtt.model.WeatherData;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.time.ZonedDateTime;
import java.util.UUID;

@Component
public class MqttService {
    private static final String serverAddress = "wss://tie.digitraffic.fi:61619/mqtt";
    private static final String username = "digitraffic";
    private static final String password = "digitrafficPassword";
    private static final String CLIENT_ID = "hass-digitraffic-";

    private static final Logger LOG = LoggerFactory.getLogger(MqttService.class);

    private final Gson gson = new GsonBuilder().registerTypeAdapter(ZonedDateTime.class, (JsonDeserializer<ZonedDateTime>) (json, type, jsonDeserializationContext) -> ZonedDateTime.parse(json.getAsJsonPrimitive().getAsString())).create();

    public MqttService() throws MqttException {
        final String clientId = CLIENT_ID + UUID.randomUUID().toString();
        final IMqttClient client = new MqttClient(serverAddress, clientId);

        client.connect(setUpConnectionOptions());
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(final Throwable cause) {

            }

            @Override
            public void messageArrived(final String topic, final MqttMessage message) throws Exception {
                try {
                    handleMessage(topic, message);
                } catch(final Exception e) {
                    LOG.error("error", e);
                }
            }

            @Override
            public void deliveryComplete(final IMqttDeliveryToken token) {

            }
        });
        client.subscribe("weather/#");

        LOG.info("Starting mqtt client");
    }

    private void handleMessage(final String topic, final MqttMessage message) {
        System.out.println(String.format("%s: topic %s, message %s", ZonedDateTime.now().toString(), topic, message));

        final WeatherData wd = gson.fromJson(new String(message.getPayload()), WeatherData.class);

        System.out.println(String.format("%s %s", wd.sensorValue, wd.measuredTime));
    }

    private static MqttConnectOptions setUpConnectionOptions() {
        final MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        connOpts.setUserName(username);
        connOpts.setPassword(password.toCharArray());
        return connOpts;
    }
}
