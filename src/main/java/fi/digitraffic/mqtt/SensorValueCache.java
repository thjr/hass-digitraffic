package fi.digitraffic.mqtt;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SensorValueCache {
    private final Map<String, CacheValue> map = new ConcurrentHashMap<>();

    private static final Duration MAX_TIME_BETWEEN = Duration.ofMinutes(1);
    private static final Duration MIN_TIME_BETWEEN = Duration.ofSeconds(60);

    public synchronized boolean checkTimeAndValue(final String key, final String value) {
        final CacheValue cv = map.get(key);

        if(cv == null || isMaxTime(cv) || hasValueChanged(cv, value)) {
            map.put(key, new CacheValue(value, ZonedDateTime.now()));

            return true;
        }

        return false;
    }

    private boolean hasValueChanged(final CacheValue cv, final String value) {
        return !cv.value.equals(value) && cv.timestamp.isBefore(ZonedDateTime.now().minus(MIN_TIME_BETWEEN));
    }

    private boolean isMaxTime(final CacheValue cv) {
        return cv.timestamp.isBefore(ZonedDateTime.now().minus(MAX_TIME_BETWEEN));
    }

    public synchronized boolean checkTime(final String key) {
        final CacheValue cv = map.get(key);

        if(cv == null || isMaxTime(cv)) {
            map.put(key, new CacheValue(null, ZonedDateTime.now()));

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
