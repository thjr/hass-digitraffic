package fi.digitraffic;

import fi.digitraffic.hass.OptionsService;
import fi.digitraffic.hass.SensorValueService;
import fi.digitraffic.mqtt.MqttOptionService;
import fi.digitraffic.mqtt.MqttService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@Import({MqttService.class, SensorValueService.class, OptionsService.class, MqttOptionService.class})
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
