package fi.digitraffic.mqtt;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SensorValueCache {
    private final Map<String, CacheValue> map = new ConcurrentHashMap<>();

    private final Duration maxTime;
    private final Duration minTime;

    public SensorValueCache(final Duration minTime, final Duration maxTime) {
        this.minTime = minTime;
        this.maxTime = maxTime;
    }

    public SensorValueCache(final Duration minTime) {
        this(minTime, minTime);
    }

    public boolean checkTime(final String key) {
        return checkTimeAndValue(key, "");
    }

    public synchronized boolean checkTimeAndValue(final String key, final String value) {
        final CacheValue cv = map.get(key);

        if(cv == null || isMaxTime(cv) || hasValueChanged(cv, value)) {
            map.put(key, new CacheValue(value, ZonedDateTime.now()));

            return true;
        }

        return false;
    }

    private boolean hasValueChanged(final CacheValue cv, final String value) {
        return !cv.value.equals(value) && cv.timestamp.isBefore(ZonedDateTime.now().minus(minTime));
    }

    private boolean isMaxTime(final CacheValue cv) {
        return cv.timestamp.isBefore(ZonedDateTime.now().minus(maxTime));
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
