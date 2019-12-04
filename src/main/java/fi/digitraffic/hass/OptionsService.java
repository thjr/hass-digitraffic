package fi.digitraffic.hass;

import com.google.gson.Gson;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.FileReader;

@Component
public class OptionsService {
    private static final String OPTIONS_FILENAME = "data/options.json";

    private final Gson gson = new Gson();

    public <T> T readOptions(final Class<T> clazz) throws FileNotFoundException {
        return gson.fromJson(new FileReader(OPTIONS_FILENAME), clazz);
    }
}
