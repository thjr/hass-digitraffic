# hass-digitraffic
==================

This Hass.io platform add-on reads data from Digitraffic and posts it to your Home Assistant instance.

Installation
------------

Easiest way is clone the repository to your hass.io-installation local addons directory and add as a local add-on.

```
mvn package
```

And the install the add-on.

Configuration
-------------

```json
{
  "sensors": [
    {
      "sensorName": "paasikiventie_temperature",
      "mqttPath": "4057/3",
      "sensorType": "weather",
      "unitOfMeasurement": "°C"
    }
  ]
}
```