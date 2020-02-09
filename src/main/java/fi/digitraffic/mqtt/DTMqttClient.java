package fi.digitraffic.mqtt;

import fi.digitraffic.mqtt.model.ConfigMap;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.UUID;

import static fi.digitraffic.mqtt.ServerConfig.*;

public class DTMqttClient {
    private static final Logger LOG = LoggerFactory.getLogger(DTMqttClient.class);

    private IMqttClient client;
    private final ServerConfig serverConfig;
    private final ConfigMap configMap;
    private final MqttService.MessageHandler messageHandler;

    private volatile int reconnect = 0;

    public DTMqttClient(final ServerConfig serverConfig, final ConfigMap configMap, final MqttService.MessageHandler messageHandler) {
        this.serverConfig = serverConfig;
        this.configMap = configMap;
        this.messageHandler = messageHandler;
    }

    private MqttCallback createCallBack(final ConfigMap configMap, IMqttClient client, final MqttService.MessageHandler messageHandler) {
        return new MqttCallback() {
            @Override
            public void connectionLost(final Throwable cause) {
                LOG.error("connection lost", cause);
                try {
                    connect();
                } catch (final MqttException e) {
                    LOG.error("can't reconnect", e);

                    if(reconnect++ > 3) {
                        System.exit(-1);
                    }
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

    private static MqttConnectOptions setUpConnectionOptions(final boolean needUsername) {
        final MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);

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
}
