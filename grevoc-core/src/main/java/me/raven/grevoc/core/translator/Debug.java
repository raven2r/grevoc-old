package me.raven.grevoc.core.translator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/** Debug translator made for test and debugging */
public class Debug extends Translator {
    public static final String ENGINE_NAME = "_debug";
    private HashSet<String> supported_languages;

    /** Constructs Debug with given language pair
     *
     * @param sl source language
     * @param tl target language
     */
    public Debug(String sl, String tl) {
        super(sl, tl);
    }


    @Override
    public HashSet<String> translate (String word) {
        var translations = new ArrayList<String>();
        var random = new Random(System.nanoTime());
        var characters = word.chars()
                .mapToObj(c -> (char)c).collect(Collectors.toList());

        translations.add(word.toUpperCase());

        // add a few mixes of generated pseudo translation
        for(int i = 1; i <= random.nextInt(9); i++){
            if(random.nextInt(2) == 1){
                var translation = new StringBuilder();
                Collections.shuffle(characters);
                characters.forEach(translation::append);
                translations.add(translation.toString());
            }
        }

        return new HashSet<String>(translations);
    }

    protected void loadLanguages() {
    }

    @Override
    public HashSet<String> getLanguages() {
        return new HashSet<String>(supported_languages);
    }
}