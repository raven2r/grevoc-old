package me.raven.grevoc.core;

import org.ini4j.Ini;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Configuration {
    private static Logger LOGGER = LogManager.getLogger();

    private boolean allowOnline = false;
    private Map<String, String> translators_API_keys = new HashMap<>();

    public Configuration() {
    }

    public void setDefaultValues() {
        allowOnline = false;
        translators_API_keys.clear();
    }

    public void readFromIni(Path ini_config_file) throws IOException {
        try {
            var ini_config = new Ini(ini_config_file.toFile());
            // CHECK no validation of fields
            ini_config.get("api_keys").forEach((k,v) -> translators_API_keys.put(k,v));
        }
        catch(IOException ioe) {
            LOGGER.error(ioe.getStackTrace());
            throw ioe;
        }
    }

    public String getAPIKey(String translator_name) {
        if(null == translator_name || "".equals(translator_name)) {
            LOGGER.warn("Null or \"\" translator name provided");
            return "";
        }
        if(!translators_API_keys.containsKey(translator_name)) {
            LOGGER.warn("No translator API key with name " + translator_name + "specified");
            return "";
        }

        return translators_API_keys.get(translator_name);
    }

    public boolean setAPIKey(String translator_name, String ak) {
        if(null == translator_name
                || ak == null
                || "".equals(translator_name)
                || "".equals(translator_name)) {
            LOGGER.warn("Provided translator_name or API key (ak) is empty (null or \"\")");
            return false;
        }
        if(translators_API_keys.get(translator_name).equals(ak)) {
            LOGGER.warn("Provided API key equals existing one, no changes will be performed");
            return false;
        }

        translators_API_keys.put(translator_name, ak);

        return true;
    }
}
