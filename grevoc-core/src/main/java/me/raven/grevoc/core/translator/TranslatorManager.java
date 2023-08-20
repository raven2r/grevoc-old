package me.raven.grevoc.core.translator;


/** Implements utility functions for translators in package
 * @see me.raven.grevoc.core.translator.Translator */
public class TranslatorManager {
    /** Constructs Translator object from engine name
     *
     * @param name name of translator engine
     * @param src_language source language
     * @param tgt_language target language
     * @return Translator of specified engine
     */
    public static Translator construct(String name, String src_language, String tgt_language) {
        switch (name) {
            case "debug":
                return new Debug(src_language, tgt_language);
            default:
                throw new IllegalStateException("Unknown translator name [" + name + "].");
        }
    }
}

