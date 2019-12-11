# hass-digitraffic
------------------

This Hass.io platform add-on reads data from Digitraffic and posts it to your Home Assistant instance.
For more information about Digitraffic see http://www.digitraffic.fi

Installation
------------

Easiest way is clone the repository to your hass.io-installation local addons directory and add as a local add-on.

```
mvn package
```

And the install the add-on.

Configuration
-------------

Currently supported are road sensors.
Find your weather-station from https://tie.digitraffic.fi/api/v3/metadata/weather-stations
Find your weather-sensor from https://tie.digitraffic.fi/api/v3/metadata/weather-sensors
Find your tms-station from https://tie.digitraffic.fi/api/v3/metadata/tms-stations
Find your tms-sensor from https://tie.digitraffic.fi/api/v3/metadata/tms-sensors

Mqttpath is {sensor type]/{stationId}/{sensorId}

```json
{
  "sensors": [
    {
      "sensorName": "paasikiventie_temperature",
      "mqttPath": "weather/4057/3",
      "sensorType": "ROAD",
      "unitOfMeasurement": "°C"
    },
    {
      "sensorName": "paasikiventie_speed",
      "mqttPath": "tms/23438/5122",
      "sensorType": "ROAD",
      "unitOfMeasurement": "km/h"
    }
  ]
}
```

Datasource
----------
Source: Traffic Management Finland / digitraffic.fi, license CC 4.0 BY

More information about attribution can be found from the Creative Commons website.