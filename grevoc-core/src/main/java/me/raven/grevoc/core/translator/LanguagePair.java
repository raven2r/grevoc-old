package me.raven.grevoc.core.translator;

import java.util.Objects;

/** LanguagePair class holds source and target languages */
public class LanguagePair {
    private final String source_language;
    private final String target_language;

    /** Constructs LanguagePair
     *
     * @param sl source language
     * @param tl target language
     */
    public LanguagePair(String sl, String tl) {
        Objects.requireNonNull(sl, "Source language has to be non null");
        Objects.requireNonNull(tl, "Target language has to be non null");

        if(sl.equals(tl))
            throw new IllegalArgumentException("Languages have to be not the same: sl=" + sl
                    + "tl=" + tl);

        source_language = sl;
        target_language = tl;
    }

    /** Returns source language */
    public String getSource() {
        return source_language;
    }

    /** Returns target language */
    public String getTarget() {
        return target_language;
    }
}
