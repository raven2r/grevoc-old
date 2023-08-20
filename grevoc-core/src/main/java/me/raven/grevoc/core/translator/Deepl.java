package me.raven.grevoc.core.translator;

import com.deepl.api.Language;
import com.deepl.api.Translator;
import com.deepl.api.DeepLException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/** Deepl class implements Deepl (deepl.com) online translator */
public class Deepl extends me.raven.grevoc.core.translator.Translator {
    public static final String ENGINE_NAME = "deepl";
    private static final Logger LOGGER = LogManager.getLogger();
    private final HashSet<String> supported_languages = new HashSet<String>();
    private final com.deepl.api.Translator deepl_translator;


    public Deepl(String sl, String tl, String api_key) {
        super(sl, tl);
        deepl_translator = new com.deepl.api.Translator(api_key);
        loadLanguages();
    }

    protected void loadLanguages() {
        LOGGER.info("Loading DeepL languages");

        try {
            List<Language> server_source_languages = deepl_translator.getSourceLanguages();
            List<Language> server_target_languages = deepl_translator.getTargetLanguages();

            supported_languages.addAll(server_target_languages.stream()
                    .filter(server_target_languages::contains).map(Language::getCode)
                    .collect(Collectors.toCollection(HashSet::new))
            );
        }
        catch(InterruptedException ie) {
            LOGGER.warn(ie);
        }
        catch(DeepLException de) {
            LOGGER.warn("DeepL error", de);
        }
    }

    @Override
    public HashSet<String> getLanguages() {
        return new HashSet<String>(supported_languages);
    }

    @Override
    public HashSet<String> translate(String word) {
        LOGGER.trace("Translating word: " + "[" + word + "]");
        HashSet<String> list_of_translations = new HashSet<>();

        try {
            list_of_translations = new HashSet<>(List.of(
                    deepl_translator.translateText(word, getSourceLanguage(), getTargetLanguage())
                            .getText()
            ));
        }
        catch(InterruptedException ie) {
            LOGGER.error(ie);
        }
        catch(DeepLException de) {
            LOGGER.error("Deepl internal problem", de);
        }

        return list_of_translations;
    }

    public void printLanguages(PrintWriter pw) {
        supported_languages.forEach(pw::println);
        pw.flush();
    }

    public void printLanguages() {
        printLanguages(new PrintWriter(System.out));
    }
}