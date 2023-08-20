package me.raven.grevoc.core.translator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.net.URISyntaxException;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

/** Lingvanex class for Lingvanex (lingvanex.com) online translator */
public class Lingvanex extends Translator {
    public static class ServerLanguage implements Comparable<ServerLanguage> {
        public static final Map<String, String> LANGUAGE_PRIORITY_MAP = Map.of(
                "ar", "ar_AE",
                "en", "en_US",
                "es", "es_ES",
                "fr", "fr_FR",
                "pt", "pt_PT"
        );

        private final String code_alpha_1;
        private final String full_code;
        private final String name;

        public ServerLanguage(String ca1, String fc, String n) {
            this.code_alpha_1 = ca1;
            this.full_code = fc;
            this.name = n;
        }

        public static void printLanguages(Iterable<ServerLanguage> languages) {
            printLanguages(languages, new PrintWriter(System.out));
        }

        public static void printLanguages(Iterable<ServerLanguage> languages, PrintWriter pw) {
            LOGGER.debug("Writing languages by given PrintWriter: " + pw);

            for(var language: languages) {
                var sb = new StringBuilder();

                sb.append(language.getCodeAlpha1()).append("\t")
                        .append(language.getFullCode()).append("\t")
                        .append('"').append(language.getName()).append('"');

                pw.println(sb.toString());
                pw.flush();
            }
        }

        public static ServerLanguage getByCode(String code,
                                               Iterable<ServerLanguage> languages) {
            if(isCodeAlpha1Format(code))
                return getByCodeAlpha1(code, languages);
            if(isFullCodeFormat(code))
                return getByFullCode(code, languages);

            LOGGER.warn("Provided code is not in correct format (full, alpha1): [" + code + "]");
            return null;
        }

        public static ServerLanguage getByCodeAlpha1(String code,
                                                     Iterable<ServerLanguage> languages) {
            if(!isCodeAlpha1Format(code))
                return null;

            if(isCollisionCodeAlpha1(code))
                return getByFullCode(LANGUAGE_PRIORITY_MAP.get(code), languages);

            for(var language: languages)
                if(language.getCodeAlpha1().equals(code))
                    return language;

            LOGGER.trace("No language to get from by given code(code_alpha_1)");
            return null;
        }

        public static ServerLanguage getByFullCode(String code,
                                                   Iterable<ServerLanguage> languages) {
            if(!isFullCodeFormat(code))
                return null;

            for(var language: languages)
                if(language.getFullCode().equals(code))
                    return language;

            LOGGER.trace("No language to get from by the given code(full_code)");
            return null;
        }

        public static boolean isSupportedCodeFormat(String code) {
            return (isCodeAlpha1Format(code) || isFullCodeFormat(code));
        }

        public static boolean isCodeAlpha1Format(String code) {
            if(!Pattern.matches("[a-z]{2}?", code)) {
                LOGGER.trace("Provided code is not in code_alpha_1 format: " + code);
                return false;
            }

            return true;
        }

        public static boolean isFullCodeFormat(String code) {
            if(Pattern.matches("[a-z]{2}?_[A-Z]{2}?", code)) {
                LOGGER.trace("Provided code is not in full_code format: " + code);
                return false;
            }

            return true;
        }

        public static boolean isCollisionCodeAlpha1(String code) {
            if(!isCodeAlpha1Format(code))
                return false;

            if(!LANGUAGE_PRIORITY_MAP.containsKey(code)) {
                LOGGER.trace("Provided code_alpha_1 is absent in priority map: [" + code + "]");
                return false;
            }

            LOGGER.trace("Collision code_alpha_1 found in priority map: [" + code + "]");
            return true;
        }

        public String getCodeAlpha1() {
            return code_alpha_1;
        }
        public String getFullCode() {
            return full_code;
        }
        public String getName() {
            return name;
        }

        @Override
        public int compareTo(ServerLanguage sl2) {
            return this.full_code.compareTo(sl2.getFullCode());
        }

        @Override
        public String toString() {
            return "Language:" + getName()
                    + ",code_alpha_1=" + getCodeAlpha1()
                    + ",full_code=" + getFullCode();
        }

        @Override
        public boolean equals(Object supported_language2) {
            var sl2 = (ServerLanguage) supported_language2;
            if(this.getCodeAlpha1().equals((sl2.getCodeAlpha1()))
                || this.getFullCode().equals(sl2.getFullCode())
                || this.getName().equals(sl2.getName())
            ) {
                LOGGER.trace(this + " equals " + sl2);
                return true;
            }

            LOGGER.trace(this + " equals not " + sl2);
            return false;
        }
    }

    public static final String ENGINE_NAME = "lingvanex";
    protected static final String API_URI_PREFIX = "https://api-b2b.backenster.com/b1/api/v3/";
    protected static final HttpClient CLIENT = HttpClient.newHttpClient();
    protected static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Logger LOGGER = LogManager.getLogger();

    private final HashSet<ServerLanguage> server_languages = new HashSet<>();
    private String api_key = "";
    private ServerLanguage source;
    private ServerLanguage target;

    Lingvanex() {
        super();
    }

    public Lingvanex(String sl, String tl, String ak) {
        super();
        setAPIKey(ak);
        loadLanguages();

        // CHECK optimization
        if(isSupportedLanguage(sl) && isSupportedLanguage(tl))
            throw new IllegalArgumentException("Source or target language is not supported");

        setSourceLanguage(sl);
        setTargetLanguage(tl);
    }

    public void loadLanguages() {
        String request_uri_postfix = "getLanguages?platform=api";

        try {
            LOGGER.info("Load request with api key: " + api_key);
            var request = HttpRequest.newBuilder()
                    .uri(new URI(API_URI_PREFIX + request_uri_postfix))
                    .header("Authorization", api_key)
                    .header("accept", "application/json")
                    .build();

            InputStream is = makeRequest(request);
            placeJSONLoadedLanguages(is);
            is.close();
        }
        catch (IOException ioe) {
            LOGGER.error("JSON language parse IOException: " + ioe);
        }
        catch(URISyntaxException urise) {
            LOGGER.error("Request URI isn't correct" + urise);
        }
    }

    void placeJSONLoadedLanguages(InputStream is) throws IOException {
        JsonNode json = MAPPER.readTree(is);
        JsonNode loaded_languages = json.findValue("result");

        loaded_languages.elements().forEachRemaining(sl ->
                this.server_languages.add( new ServerLanguage(
                        sl.get("code_alpha_1").asText(),
                        sl.get("full_code").asText(),
                        sl.get("englishName").asText())
                )
        );
    }

    private InputStream makeRequest(HttpRequest request) {
        try {

            var response = CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
            return response.body();
        }
        catch(IOException ioe) {
            LOGGER.error("IO error:\n", ioe);
        }
        catch(InterruptedException ie) {
            LOGGER.error("Connection has been interrupted:\n", ie);
        }

        LOGGER.error("Couldn't make InputStream from request");
        return InputStream.nullInputStream();
    }

    public boolean isSupportedLanguage(String language_code) {
        if((ServerLanguage.isCodeAlpha1Format(language_code)
                && null != ServerLanguage.getByCodeAlpha1(language_code, server_languages))
            || ServerLanguage.isFullCodeFormat(language_code)
                && null != ServerLanguage.getByFullCode(language_code, server_languages)) {

            LOGGER.trace("Lingvanex supports provided language: " + language_code);
            return true;
        }

        LOGGER.trace("Lingvanex doesn't support provided language: " + language_code);
        return false;
    }

    public boolean isSupportedLanguage(ServerLanguage sl) {
        if(server_languages.isEmpty())
            throw new IllegalStateException("Languages are not loaded");

        if(server_languages.contains(sl)) {
            LOGGER.trace("Provided language is supported: " + sl);
            return true;
        }

        LOGGER.trace("Provided language is NOT supported: " + sl);
        return false;
    }

    @Override
    public HashSet<String> getLanguages() {
        var languagesSet = new HashSet<String>();
        server_languages.forEach(sl -> languagesSet.add(sl.getCodeAlpha1()));
        return languagesSet;
    }

    @Override
    public HashSet<String> translate(String word) {
        String request_uri_postfix = "/translate";

        ObjectNode post_request_json_body =
                MAPPER.createObjectNode()
                        .put("translateMode", "html")
                        .put("platform", "api")
                        .put("from", source.getFullCode())
                        .put("to", target.getFullCode())
                        .put("data", word);

        try {
            var request = HttpRequest.newBuilder()
                    .uri(new URI(API_URI_PREFIX + request_uri_postfix))
                    .header("Authorization", api_key)
                    .header("accept", "application/json")
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(post_request_json_body.asText()))
                    .build();
        }
        catch (URISyntaxException urise) {
            LOGGER.error("Translate request URI is broken: ", urise);
        }

        return new HashSet<String>();
    }

    public HashSet<ServerLanguage> getSupportedLanguages() {
        return new HashSet<ServerLanguage>(server_languages);
    }

    public boolean setAPIKey(String ak) {
        this.api_key = ak;
        return true;
    }

    public void verifyInitializationCompleteness() {
        IllegalStateException ise;

        if(api_key.equals("")) {
            ise = new IllegalStateException("API key is empty");
            LOGGER.error(ise);
            throw ise;
        }
        if(getSourceLanguage().equals("")) {
            ise = new IllegalStateException("Source language is not defined");
            LOGGER.error(ise);
            throw ise;
        }
        if(getTargetLanguage().equals("")) {
            ise = new IllegalStateException("Target language is not defined");
            LOGGER.error(ise);
            throw ise;
        }
        if(server_languages.isEmpty()) {
            ise = new IllegalStateException("Supported languages are not loaded");
            LOGGER.error(ise);
            throw ise;
        }
    }

    @Override
    public boolean setSourceLanguage(String language_code) {
        if(isSupportedLanguage(language_code) && !isLanguageAlreadyPresent(language_code)) {
            var new_language = ServerLanguage.getByCode(language_code, server_languages);
            source = new_language;
            super.setSourceLanguage(new_language.getCodeAlpha1());
            LOGGER.trace("New source language set: " + new_language);
            return true;
        }

        LOGGER.trace("Provided language will not be set as source: " + language_code);
        return false;
    }

    @Override
    public boolean setTargetLanguage(String language_code) {
        if(isSupportedLanguage(language_code) && !isLanguageAlreadyPresent(language_code)) {
            var new_language = ServerLanguage.getByCode(language_code, server_languages);
            target = new_language;
            super.setTargetLanguage(target.getCodeAlpha1());
            LOGGER.trace("New target language set: " + new_language);
            return true;
        }

        LOGGER.trace("Provided language will not be set as target: " + language_code);
        return false;
    }

    public ServerLanguage getSource() {
        return source;
    }

    public ServerLanguage getTarget() {
        return target;
    }

    public boolean isLanguageAlreadyPresent(String language_code) {
        return isLanguageAlreadyPresent(
                ServerLanguage.getByCode(language_code, server_languages));
    }

    public boolean isLanguageAlreadyPresent(ServerLanguage sl) {
        if(sl.equals(source)) {
            LOGGER.trace("Language is already set as source language: " + source);
            return true;
        }
        else if(sl.equals(target)) {
            LOGGER.trace("Language is already set as target language: " + target);
            return true;
        }

        LOGGER.trace("Language is not source nor target: " + sl);
        return false;
    }
}