package me.raven.grevoc.core.translator;

import com.fasterxml.jackson.databind.annotation.JsonAppend;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class DeeplTest {
    private static Deepl makeDeeplTestObject() {
        Path api_keys_file = Path.of("src", "test", "resources", "keys.properties");
        String api_key = "";
        var api_key_properties = new Properties();

        try {
            api_key_properties.load(new FileInputStream(api_keys_file.toFile()));
            api_key = api_key_properties.getProperty("deepl");
        }
        catch (IOException e) {
        }

        assertNotEquals("", api_key);
        return new Deepl("ru", "en", api_key);
    }

    @Test
    void loadLanguages() {
    }

    @Test
    void translate() {
    }
}