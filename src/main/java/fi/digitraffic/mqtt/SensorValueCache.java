package fi.digitraffic.mqtt;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SensorValueCache {
    private final Map<String, CacheValue> map = new ConcurrentHashMap<>();

    public synchronized boolean updateValue(final String key, final String value) {
        final CacheValue cv = map.get(key);

        if(cv == null || cv.timestamp.isBefore(ZonedDateTime.now().minusMinutes(1))) {
            map.put(key, new CacheValue(value, ZonedDateTime.now()));

            return true;
        }

        return false;
    }

    private class CacheValue {
        public final String value;
        public final ZonedDateTime timestamp;

        private CacheValue(final String value, final ZonedDateTime timestamp) {
            this.value = value;
            this.timestamp = timestamp;
        }
    }
}
