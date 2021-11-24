package fi.digitraffic.mqtt;

import fi.digitraffic.mqtt.model.ConfigMap;
import org.eclipse.paho.client.mqttv3.*;
import io.quarkus.logging.Log;
import java.util.UUID;

import static fi.digitraffic.mqtt.ServerConfig.*;

public class DTMqttClient {
    private IMqttClient client;
    private final ServerConfig serverConfig;
    private final ConfigMap configMap;
    private final MqttService.MessageHandler messageHandler;

    public DTMqttClient(final ServerConfig serverConfig, final ConfigMap configMap, final MqttService.MessageHandler messageHandler) {
        this.serverConfig = serverConfig;
        this.configMap = configMap;
        this.messageHandler = messageHandler;
    }

    private void subscribe(final String topic) {
        try {
            Log.debugf("subscribing to %s", topic);
            client.subscribe(topic);
        } catch (final MqttException e) {
            Log.error(String.format("Could not subscribe to topic %s", topic), e);
        }
    }

    private MqttCallback createCallBack(final ConfigMap configMap, IMqttClient client, final MqttService.MessageHandler messageHandler) {
        return new MqttCallbackExtended() {
            @Override
            public void connectComplete(final boolean reconnect, final String server) {
                Log.debugf("Connection complete to %s", server);

                configMap.keys().forEach(DTMqttClient.this::subscribe);

                if(serverConfig.statusTopic != null) {
                    subscribe(serverConfig.statusTopic);
                }
            }

            @Override
            public void connectionLost(final Throwable cause) {
                Log.error("connection lost", cause);
            }

            @Override
            public void messageArrived(final String topic, final MqttMessage message) {
                if(!isStatusTopic(topic)) {
                    try {
                        messageHandler.handleMessage(message.toString(), configMap.getConfigForTopic(topic));
                    } catch (final Exception e) {
                        Log.error("error", e);
                    }
                }
            }

            @Override
            public void deliveryComplete(final IMqttDeliveryToken iMqttDeliveryToken) {
                // yeah ok
            }
        };
    }

    private static boolean isStatusTopic(final String topic) {
        return topic.contains("status");
    }

    private static MqttConnectOptions setUpConnectionOptions(final boolean needUsername) {
        final MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setAutomaticReconnect(true);

        if(needUsername) {
            connOpts.setUserName(USERNAME);
            connOpts.setPassword(PASSWORD.toCharArray());
        }

        return connOpts;
    }

    public IMqttClient connect() throws MqttException {
        final String clientId = CLIENT_ID + UUID.randomUUID().toString();
        this.client = new MqttClient(serverConfig.serverAddress, clientId);

        client.setCallback(createCallBack(configMap, client, messageHandler));
        client.connect(setUpConnectionOptions(serverConfig.needUsername));

        Log.info("Starting mqtt client " + serverConfig.serverAddress);

        return client;
    }
}
