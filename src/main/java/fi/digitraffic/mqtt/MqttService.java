package fi.digitraffic.mqtt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import fi.digitraffic.Options;
import fi.digitraffic.hass.OptionsService;
import fi.digitraffic.hass.SensorValueService;
import fi.digitraffic.mqtt.model.MqttData;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class MqttService {
    private static final String serverAddress = "wss://tie.digitraffic.fi:61619/mqtt";
    private static final String USERNAME = "digitraffic";
    private static final String PASSWORD = "digitrafficPassword";
    private static final String CLIENT_ID = "hass-digitraffic-";

    private static final Logger LOG = LoggerFactory.getLogger(MqttService.class);

    private final Gson gson = new GsonBuilder().registerTypeAdapter(ZonedDateTime.class, (JsonDeserializer<ZonedDateTime>) (json, type, jsonDeserializationContext) -> ZonedDateTime.parse(json.getAsJsonPrimitive().getAsString())).create();

    private final SensorValueService sensorValueService;
    private final OptionsService optionsService;

    public MqttService(final SensorValueService sensorValueService, OptionsService optionsService) throws MqttException, FileNotFoundException {
        this.sensorValueService = sensorValueService;
        this.optionsService = optionsService;

        initialize();
    }

    private void initialize() throws FileNotFoundException, MqttException {
        final Options options = optionsService.readOptions(Options.class);

        createClient(options);
    }

    private void createClient(Options options) throws MqttException {
        final String clientId = CLIENT_ID + UUID.randomUUID().toString();
        final IMqttClient client = new MqttClient(serverAddress, clientId);
        final Map<String, Options.SensorOption> optionsMap = options.sensors.stream().collect(Collectors.toMap(s -> s.getTopic(), s -> s));

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
            public void messageArrived(final String topic, final MqttMessage message) throws Exception {
                try {
                    if(!topic.contains("status")) {
                        LOG.info("topic {} got message {}", topic, new String(message.getPayload()));

                        handleMessage(message, optionsMap.get(topic));
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

        optionsMap.values().forEach(option -> {
            final String topic = option.getTopic();

            if(topic.contains("%") || topic.contains("*")) {
                LOG.error("wildchars are forbidden! {}", topic);
            } else {
                try {
                    LOG.info("subscribing to {}", topic);
                    client.subscribe(topic);
                } catch (final MqttException e) {
                    LOG.error(String.format("Could not not subscribe to topic %s", topic), e);
                }
            }
        });

        client.subscribe("weather/status");

        LOG.info("Starting mqtt client");
    }

    private void handleMessage(final MqttMessage message, final Options.SensorOption option) throws IOException {
        final String value;
        final String unitOfMeasurement = option.unitOfMeasurement;

        if(option.sensorType == Options.SensorType.WEATHER || option.sensorType == Options.SensorType.TMS) {
            final MqttData wd = gson.fromJson(new String(message.getPayload()), MqttData.class);
            value = wd.sensorValue;
        } else {
            throw new IllegalArgumentException("unhandled sensortype " + option.sensorType);
        }

        final int httpCode = sensorValueService.postSensorValue(option.sensorName, value, unitOfMeasurement);

        if(httpCode == 200) {
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
