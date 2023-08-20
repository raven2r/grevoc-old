package me.raven.grevoc.core.translator;

import me.raven.grevoc.core.translator.Lingvanex.ServerLanguage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Files;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

class LingvanexTest {
    private static final Path API_KEYS_FILE = Path.of(
            "src", "test", "resources", "apikeys.properties");
    private static final Path LANGUAGES_RESPONSE_JSON_FILE = Path.of(
            "src", "test", "resources", "languages-response.json");
    private String api_key = "";

    private Lingvanex makeDebugLingvanexObject() {
        var lingvanex = new Lingvanex();
        lingvanex.setSourceLanguage("en");
        lingvanex.setTargetLanguage("ru");
        lingvanex.setAPIKey("_debug");

        try(InputStream is = Files.newInputStream(LANGUAGES_RESPONSE_JSON_FILE)) {
            lingvanex.placeJSONLoadedLanguages(is);
        }
        catch(IOException ioe) {
            ioe.printStackTrace();
        }

        return lingvanex;
    }

    public void loadAPIKey() {
        try (InputStream is = Files.newInputStream(API_KEYS_FILE)) {
            Properties properties = new Properties();
            properties.load(is);
            System.out.println("Properties: " + properties);
            api_key = properties.getProperty(Lingvanex.ENGINE_NAME);
        }
        catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private List<ServerLanguage> findAlphaCode1Collisions() {
        List<ServerLanguage> sl_col = List.of();

        try {
            var lingvanex = makeDebugLingvanexObject();
            var supported_languages = lingvanex.getSupportedLanguages();
            var list_of_alpha_codes_1 = new ArrayList<String>();
            supported_languages.forEach(sl -> list_of_alpha_codes_1.add(sl.getCodeAlpha1()));
            Collections.sort(list_of_alpha_codes_1);
            var iterator = list_of_alpha_codes_1.iterator();

            System.out.println("Provided languages by alpha codes: " + list_of_alpha_codes_1);

            var col_languages = new HashSet<String>();
            var collisions = new HashMap<String, ServerLanguage>();
            String cur;
            String next;

            if(iterator.hasNext())
                cur = iterator.next();
            else
                throw new IllegalStateException("No languages in list");

            while(iterator.hasNext()) {
                next = iterator.next();

                if(next.equals(cur))
                    col_languages.add(cur);

                cur = next;
            }

            sl_col = supported_languages.stream()
                    .filter(sl -> col_languages.contains(sl.getCodeAlpha1())).sorted().toList();
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return sl_col;
    }

    private void printLanguages(List<ServerLanguage> languages) {
    }

    @Test
    public void checkByCountForAlphaCode1CollisionsDefinition() {
        List<ServerLanguage> collision_languages = findAlphaCode1Collisions();
        System.out.println("Collision languages:");
        ServerLanguage.printLanguages(collision_languages, new PrintWriter(System.out));

        Map<String, List<ServerLanguage>> grouped_collision_languages = collision_languages
                .stream()
                .collect(Collectors.groupingBy(ServerLanguage::getCodeAlpha1));

        grouped_collision_languages.forEach((key, value) -> assertAll("heading",
                () -> assertTrue(value.size() > 1)
        ));
    }

    @Test
    public void checkCodeFormatVerifiers() {
        assertTrue(Lingvanex.ServerLanguage.isCodeAlpha1Format("en"));
        assertTrue(Lingvanex.ServerLanguage.isCodeAlpha1Format("jp"));
        assertTrue(Lingvanex.ServerLanguage.isCodeAlpha1Format("ru"));
        assertFalse(Lingvanex.ServerLanguage.isCodeAlpha1Format("))"));
        assertFalse(Lingvanex.ServerLanguage.isCodeAlpha1Format("FF"));
        assertFalse(Lingvanex.ServerLanguage.isCodeAlpha1Format("--"));

        assertTrue(Lingvanex.ServerLanguage.isFullCodeFormat("en_US"));
        assertTrue(Lingvanex.ServerLanguage.isFullCodeFormat("en_US"));
    }

    //@Test
    public void checkTranslation() {
        loadAPIKey();
        var lingvanex = new Lingvanex("en", "de", api_key);
        System.out.println(lingvanex.translate("disorder"));
    }
}