package fi.digitraffic;

import fi.digitraffic.hass.ConfigService;
import fi.digitraffic.hass.SensorValueService;
import fi.digitraffic.mqtt.MqttConfigService;
import fi.digitraffic.mqtt.MqttService;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Import;

@Import({MqttService.class, SensorValueService.class, ConfigService.class, MqttConfigService.class})
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
