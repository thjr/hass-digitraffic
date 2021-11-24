package fi.digitraffic.mqtt;

import com.google.gson.*;
import fi.digitraffic.Config;
import fi.digitraffic.hass.SensorValueService;
import fi.digitraffic.mqtt.model.MqttConfig;
import fi.digitraffic.mqtt.model.MqttSensorValue;
import io.quarkus.runtime.Startup;
import org.eclipse.paho.client.mqttv3.MqttException;
import io.quarkus.logging.Log;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.concurrent.Callable;

import static fi.digitraffic.mqtt.ServerConfig.*;

@ApplicationScoped
@Startup
public class MqttService {
    private final Gson gson = new GsonBuilder().registerTypeAdapter(ZonedDateTime.class, (JsonDeserializer<ZonedDateTime>) (json, type, jsonDeserializationContext) -> ZonedDateTime.parse(json.getAsJsonPrimitive().getAsString())).create();

    private final SensorValueService sensorValueService;
    private final MqttConfigService mqttConfigService;

    private final SensorValueCache sensorValueCache = new SensorValueCache(Duration.ofSeconds(60), Duration.ofMinutes(5)); // if value changes, change value every 60 seconds at max
    private final SensorValueCache locationCache = new SensorValueCache(Duration.ofMinutes(1)); // change value once a minute, no matter what the value

    public MqttService(final SensorValueService sensorValueService, final MqttConfigService mqttConfigService) throws MqttException {
        this.sensorValueService = sensorValueService;
        this.mqttConfigService = mqttConfigService;
    }

    @PostConstruct
    void initializeAllClients() throws MqttException {
        final MqttConfig options = mqttConfigService.readAndValidate();

        if(options != null) {
            if(!options.getRoadConfigs().isEmpty()) {
                new DTMqttClient(ROAD, options.getRoadConfigs(), this::handleRoadMessage).connect();
            }
            if(!options.getSseConfigs().isEmpty()) {
                new DTMqttClient(MARINE, options.getSseConfigs(), this::handleSseMessage).connect();
            }
            if(!options.getVesselLocationConfigs().isEmpty()) {
                new DTMqttClient(MARINE, options.getVesselLocationConfigs(), this::handleVesselLocationMessage).connect();
            }
            if(!options.getTrainGpsConfigs().isEmpty()) {
                new DTMqttClient(RAIL, options.getTrainGpsConfigs(), this::handleTrainGpsMessage).connect();
            }
        }
    }

    public interface MessageHandler {
        void handleMessage(final String message, final Config.SensorConfig config);
    }

    private void handleRoadMessage(final String message, final Config.SensorConfig sensorConfig) {
        final MqttSensorValue wd = gson.fromJson(message, MqttSensorValue.class);

        // only send changes or once a minute
        if(sensorValueCache.checkTimeAndValue(sensorConfig.sensorName, wd.sensorValue)) {
            postSensorValue(sensorConfig.sensorName, wd.sensorValue, sensorConfig.unitOfMeasurement);
        }
    }

    private void handleSseMessage(final String message, final Config.SensorConfig sensorConfig) {
        final JsonObject root = JsonParser.parseString(message).getAsJsonObject();

        final JsonObject properties = root.getAsJsonObject("properties");
        final String value = properties.get(sensorConfig.propertyName).getAsString();

        postSensorValue(sensorConfig.sensorName, value, sensorConfig.unitOfMeasurement);
    }

    private void handleVesselLocationMessage(final String message, final Config.SensorConfig sensorConfig) {
        final JsonObject root = JsonParser.parseString(message).getAsJsonObject();

        final JsonObject geometry = root.getAsJsonObject("geometry");
        final JsonArray coordinates = geometry.getAsJsonArray("coordinates");
        final String longitude = coordinates.get(0).getAsString();
        final String latitude = coordinates.get(1).getAsString();

        final JsonObject properties = root.getAsJsonObject("properties");
        final String navStat = properties.get("navStat").getAsString();
        final String heading = properties.get("heading").getAsString();
        final String sog = properties.get("sog").getAsString();

        if(locationCache.checkTime(sensorConfig.sensorName)) {
            postLocation(sensorConfig.sensorName, latitude, longitude, navStat, heading, sog);
        }
    }

    private void handleTrainGpsMessage(final String message, final Config.SensorConfig config) {
        final JsonObject root = JsonParser.parseString(message).getAsJsonObject();

        final JsonObject location = root.getAsJsonObject("location");
        final JsonArray coordinates = location.getAsJsonArray("coordinates");
        final String longitude = coordinates.get(0).getAsString();
        final String latitude = coordinates.get(1).getAsString();

        final String speed = root.get("speed").getAsString();

        if(locationCache.checkTime(config.sensorName)) {
            postTrainGps(config.sensorName, latitude, longitude, speed);
        }
    }

    private void doPost(final Callable c) {
        try {
            c.call();
        } catch(final Exception e) {
            Log.error("exception from post", e);
        }
    }

    private void postSensorValue(final String sensorName, final String value, final String unitOfMeasurement) {
        doPost(() -> sensorValueService.postSensorValue(sensorName, value, unitOfMeasurement));
    }

    private void postLocation(final String entityName, final String latitude, final String longitude,
                              final String navStat, final String heading, final String sog) {
        doPost(() -> sensorValueService.postLocation(entityName, latitude, longitude, navStat, heading, sog));
    }

    private void postTrainGps(final String entityName, final String latitude, final String longitude, final String speed) {
        doPost(() -> sensorValueService.postTrainGps(entityName, latitude, longitude, speed));
    }
}
