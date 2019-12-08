package fi.digitraffic.mqtt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import fi.digitraffic.Config;
import fi.digitraffic.hass.SensorValueService;
import fi.digitraffic.mqtt.model.MqttData;
import fi.digitraffic.mqtt.model.MqttConfig;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.UUID;

import static java.net.HttpURLConnection.HTTP_OK;

@Component
public class MqttService {
    private static final String serverAddress = "wss://tie.digitraffic.fi:61619/mqtt";
    private static final String USERNAME = "digitraffic";
    private static final String PASSWORD = "digitrafficPassword";
    private static final String CLIENT_ID = "hass-digitraffic-";

    private static final Logger LOG = LoggerFactory.getLogger(MqttService.class);

    private final Gson gson = new GsonBuilder().registerTypeAdapter(ZonedDateTime.class, (JsonDeserializer<ZonedDateTime>) (json, type, jsonDeserializationContext) -> ZonedDateTime.parse(json.getAsJsonPrimitive().getAsString())).create();

    private final SensorValueService sensorValueService;
    private final MqttConfigService mqttConfigService;

    public MqttService(final SensorValueService sensorValueService, final MqttConfigService mqttConfigService) throws MqttException {
        this.sensorValueService = sensorValueService;
        this.mqttConfigService = mqttConfigService;

        initialize();
    }

    private void initialize() throws MqttException {
        final MqttConfig options = mqttConfigService.readAndValidate();

        if(options != null) {
            createClient(options);
        }
    }

    private void createClient(final MqttConfig config) throws MqttException {
        final String clientId = CLIENT_ID + UUID.randomUUID().toString();
        final IMqttClient client = new MqttClient(serverAddress, clientId);

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(final Throwable cause) {
                LOG.error("connection lost", cause);
                try {
                    client.reconnect();
                } catch (final MqttException e) {
                    LOG.error("can't reconnect", e);
                }
            }

            @Override
            public void messageArrived(final String topic, final MqttMessage message) {
                try {
                    if(!topic.contains("status")) {
                        LOG.info("topic {} got message {}", topic, new String(message.getPayload()));

                        handleRoadMessage(message, config.getOption(topic));
                    }
                } catch(final Exception e) {
                    LOG.error("error", e);
                }
            }

            @Override
            public void deliveryComplete(final IMqttDeliveryToken token) {

            }
        });
        client.connect(setUpConnectionOptions());

        config.getRoadTopics().forEach(topic -> {
            try {
                LOG.info("subscribing to {}", topic);
                client.subscribe(topic);
            } catch (final MqttException e) {
                LOG.error(String.format("Could not not subscribe to topic %s", topic), e);
            }
        });

        client.subscribe("weather/status");

        LOG.info("Starting mqtt client");
    }

    private void handleRoadMessage(final MqttMessage message, final Config.SensorConfig sensorConfig) throws IOException {
        final String unitOfMeasurement = sensorConfig.unitOfMeasurement;
        final MqttData wd = gson.fromJson(new String(message.getPayload()), MqttData.class);
        final String value = wd.sensorValue;

        final int httpCode = sensorValueService.postSensorValue(sensorConfig.sensorName, value, unitOfMeasurement);

        if(httpCode != HTTP_OK) {
            LOG.error("post sensor value returned {}", httpCode);
        }
    }

    private static MqttConnectOptions setUpConnectionOptions() {
        final MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        connOpts.setUserName(USERNAME);
        connOpts.setPassword(PASSWORD.toCharArray());
        return connOpts;
    }
}
