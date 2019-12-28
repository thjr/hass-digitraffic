package fi.digitraffic.mqtt;

import com.google.gson.*;
import fi.digitraffic.Config;
import fi.digitraffic.hass.SensorValueService;
import fi.digitraffic.mqtt.model.ConfigMap;
import fi.digitraffic.mqtt.model.MqttConfig;
import fi.digitraffic.mqtt.model.MqttSensorValue;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static fi.digitraffic.mqtt.ServerConfig.*;
import static java.net.HttpURLConnection.HTTP_OK;

@Component
public class MqttService {
    private static final Logger LOG = LoggerFactory.getLogger(MqttService.class);

    private final Gson gson = new GsonBuilder().registerTypeAdapter(ZonedDateTime.class, (JsonDeserializer<ZonedDateTime>) (json, type, jsonDeserializationContext) -> ZonedDateTime.parse(json.getAsJsonPrimitive().getAsString())).create();

    private final SensorValueService sensorValueService;
    private final MqttConfigService mqttConfigService;

    private final List<IMqttClient> clientList = new ArrayList();

    public MqttService(final SensorValueService sensorValueService, final MqttConfigService mqttConfigService) throws MqttException {
        this.sensorValueService = sensorValueService;
        this.mqttConfigService = mqttConfigService;

        initializeAllClients();
    }

    private void initializeAllClients() throws MqttException {
        final MqttConfig options = mqttConfigService.readAndValidate();

        if(options != null) {
            if(!options.getRoadConfigs().isEmpty()) {
                clientList.add(createClient(options.getRoadConfigs(), ServerConfig.ROAD, this::handleRoadMessage));
            }
            if(!options.getSseConfigs().isEmpty()) {
                clientList.add(createClient(options.getSseConfigs(), ServerConfig.MARINE, this::handleSseMessage));
            }
            if(!options.getVesselLocationConfigs().isEmpty()) {
                clientList.add(createClient(options.getVesselLocationConfigs(), ServerConfig.MARINE, this::handleVesselLocationMessage));
            }
            if(!options.getTrainGpsConfigs().isEmpty()) {
                clientList.add(createClient(options.getTrainGpsConfigs(), RAIL, this::handleTrainGpsMessage));
            }
        }
    }

    private void closeAllClients() {
        for (final IMqttClient iMqttClient : clientList) {
            try {
                iMqttClient.disconnectForcibly();
                iMqttClient.close();
            } catch(final Exception e) {
                LOG.error("error when closing connection", e);
            }
        }

        clientList.clear();
    }

    private interface MessageHandler {
        void handleMessage(final String message, final Config.SensorConfig config);
    }

    private boolean reconnect(final IMqttClient client) {
        try {
            client.reconnect();
        } catch (final MqttException e) {
            LOG.error("reconnect failed");
            return false;
        }

        return true;
    }

    private MqttCallback createCallBack(final ConfigMap configMap, IMqttClient client, final MessageHandler messageHandler) {
        return new MqttCallback() {
            @Override
            public void connectionLost(final Throwable cause) {
                LOG.error("connection lost", cause);
                try {
                    if(!reconnect(client)) {
                        closeAllClients();
                        initializeAllClients();
                    }
                } catch (final MqttException e) {
                    LOG.error("can't reconnect", e);
                }
            }

            @Override
            public void messageArrived(final String topic, final MqttMessage message) {
                try {
                    if(!topic.contains("status")) {
                        messageHandler.handleMessage(message.toString(), configMap.getConfigForTopic(topic));
                    }
                } catch(final Exception e) {
                    LOG.error("error", e);
                }
            }

            @Override
            public void deliveryComplete(final IMqttDeliveryToken token) {
                // Do nothing
            }
        };
    }

    private IMqttClient createClient(final ConfigMap configMap, final ServerConfig serverConfig, final MessageHandler messageHandler) throws MqttException {
        final String clientId = CLIENT_ID + UUID.randomUUID().toString();
        final IMqttClient client = new MqttClient(serverConfig.serverAddress, clientId);

        client.setCallback(createCallBack(configMap, client, messageHandler));
        client.connect(setUpConnectionOptions(serverConfig.needUsername));

        configMap.keys().forEach(topic -> {
            try {
                LOG.info("subscribing to {}", topic);
                client.subscribe(topic);
            } catch (final MqttException e) {
                LOG.error(String.format("Could not not subscribe to topic %s", topic), e);
            }
        });

        if(!StringUtils.isEmpty(serverConfig.statusTopic)) {
            client.subscribe(serverConfig.statusTopic);
        }

        LOG.info("Starting mqtt client " + serverConfig.serverAddress);

        return client;
    }

    private void handleRoadMessage(final String message, final Config.SensorConfig sensorConfig) {
        final MqttSensorValue wd = gson.fromJson(message, MqttSensorValue.class);

        postSensorValue(sensorConfig.sensorName, wd.sensorValue, sensorConfig.unitOfMeasurement);
    }

    private void handleSseMessage(final String message, final Config.SensorConfig sensorConfig) {
        final JsonParser parser = new JsonParser();
        final JsonObject root = parser.parse(message).getAsJsonObject();

        final JsonObject properties = root.getAsJsonObject("properties");
        final String value = properties.get(sensorConfig.propertyName).getAsString();

        postSensorValue(sensorConfig.sensorName, value, sensorConfig.unitOfMeasurement);
    }

    private void handleVesselLocationMessage(final String message, final Config.SensorConfig sensorConfig) {
        final JsonParser parser = new JsonParser();
        final JsonObject root = parser.parse(message).getAsJsonObject();

        final JsonObject geometry = root.getAsJsonObject("geometry");
        final JsonArray coordinates = geometry.getAsJsonArray("coordinates");
        final String longitude = coordinates.get(0).getAsString();
        final String latitude = coordinates.get(1).getAsString();

        final JsonObject properties = root.getAsJsonObject("properties");
        final String navStat = properties.get("navStat").getAsString();
        final String heading = properties.get("heading").getAsString();
        final String sog = properties.get("sog").getAsString();

        postLocation(sensorConfig.sensorName, latitude, longitude, navStat, heading, sog);
    }

    private void handleTrainGpsMessage(final String message, final Config.SensorConfig config) {
        final JsonParser parser = new JsonParser();
        final JsonObject root = parser.parse(message).getAsJsonObject();

        final JsonObject location = root.getAsJsonObject("location");
        final JsonArray coordinates = location.getAsJsonArray("coordinates");
        final String longitude = coordinates.get(0).getAsString();
        final String latitude = coordinates.get(1).getAsString();

        final String speed = root.get("speed").getAsString();

        postTrainGps(config.sensorName, latitude, longitude, speed);
    }

    private void postSensorValue(final String sensorName, final String value, final String unitOfMeasurement) {
        try {
            final int httpCode = sensorValueService.postSensorValue(sensorName, value, unitOfMeasurement);

            if(httpCode != HTTP_OK) {
                LOG.error("post sensor value returned {}", httpCode);
            }
        } catch(final Exception e) {
            LOG.error("exception from post", e);
        }
    }

    private void postLocation(final String entityName, final String latitude, final String longitude,
                              final String navStat, final String heading, final String sog) {
        try {
            final int httpCode = sensorValueService.postLocation(entityName, latitude, longitude, navStat, heading, sog);

            if(httpCode != HTTP_OK) {
                LOG.error("post sensor value returned {}", httpCode);
            }
        } catch(final Exception e) {
            LOG.error("exception from post", e);
        }
    }

    private void postTrainGps(final String entityName, final String latitude, final String longitude, final String speed) {
        try {
            final int httpCode = sensorValueService.postTrainGps(entityName, latitude, longitude, speed);

            if(httpCode != HTTP_OK) {
                LOG.error("post sensor value returned {}", httpCode);
            }
        } catch(final Exception e) {
            LOG.error("exception from post", e);
        }

    }

    private static MqttConnectOptions setUpConnectionOptions(final boolean needUsername) {
        final MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);

        if(needUsername) {
            connOpts.setUserName(USERNAME);
            connOpts.setPassword(PASSWORD.toCharArray());
        }

        return connOpts;
    }
}
