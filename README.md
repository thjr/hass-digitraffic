# hass-digitraffic
------------------

This Hass.io platform add-on reads data from Digitraffic and posts it to your Home Assistant instance.
For more information about Digitraffic see http://www.digitraffic.fi

Installation
------------

Easiest way is clone the repository to your hass.io-installation local addons directory(e.g. /usr/share/hassio/addons/local/) 
and add as a local add-on.

```
mvn package
```

And then install the add-on.

Configuration
-------------

Currently supported are weather/tms-sensors, sse-attributes, ais-locations and train-locations.

Example configuration, with all supported sensors configured:

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
    },
    {
      "sensorName": "kelloniemi_temperature",
      "mqttPath": "sse/site/8659",
      "sensorType": "SSE",
      "unitOfMeasurement": "°C",
      "propertyName": "temperature"
    },
    {
      "sensorName": "ais_test",
      "mqttPath": "vessels/308803000/locations",
      "sensorType": "VESSEL_LOCATION"
    },
    {
      "sensorName": "train_test",
      "mqttPath": "train-locations/+/29",
      "sensorType": "TRAIN_GPS"
    }
  ]
}
```

Datasource
----------
Source: Traffic Management Finland / digitraffic.fi, license CC 4.0 BY

More information about attribution can be found from the Creative Commons website.