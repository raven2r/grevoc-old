package me.raven.grevoc.core.translator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Translator class to generalize translators used for 'grevoc' */
public abstract class Translator {
    private static Logger LOGGER = LogManager.getLogger();

    // Online translation engines has limited capacity of words per price. Word translated by the
    // same engine in different instances must be marked so to be not translated multiple times.
    public static final String ENGINE_NAME = null;
    private String source_language = "";
    private String target_language = "";

    /** Translates single word
     * @param word to translate from source language
     * @return translated word in target language
     * @throws IllegalStateException if there is a problem with source or target language
     */
    abstract public HashSet<String> translate(String word);

    /** Puts language pairs to Map language_pairs */
    abstract public HashSet<String> getLanguages();

    /** Constructs empty Translator */
    protected Translator() {
    }

    /** Constructs Translator
     * @param sl source language
     * @param tl target language
     * @throws IllegalArgumentException throws exception if languages are same
     */
    public Translator(String sl, String tl) {
        this();

        if (sl.equals(tl))
            throw new IllegalStateException("Source language is the same as target ["
                    + sl + "=" + tl + "].");

        setSourceLanguage(sl);
        setTargetLanguage(tl);
    }


    /** Sets source language to which specified in argument
     *
     * @param language source language
     * @return returns true if target language has been set, otherwise returns false
     */
    protected boolean setSourceLanguage(String language) {
        if(language.equals(target_language)) {
            LOGGER.warn("Provided language is the same as target language: " + language);
            return false;
        }

        source_language = language;
        return true;
    }

    /** Sets target language to which specified in argument
     *
     * @param language source language
     * @return returns true if target language has been set, otherwise returns false
     */
    protected boolean setTargetLanguage(String language) {
        if (language.equals(source_language)) {
            LOGGER.warn("Provided language is the same as source language: " + language);
            return false;
        }

        target_language = language;
        return true;
    }

    public String getSourceLanguage() {
        return source_language;
    }
    public String getTargetLanguage() {
        return target_language;
    }

    @Override
    public String toString() {
        return super.toString() + ", sl=" + source_language + " tl=" + target_language;
    }
}