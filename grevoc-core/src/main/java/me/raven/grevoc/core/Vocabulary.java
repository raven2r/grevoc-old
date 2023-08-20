package me.raven.grevoc.core;


import me.raven.grevoc.core.translator.LanguagePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.io.PrintWriter;

import java.io.IOException;


/** Vocabulary class for 'grevoc' vocabulary project
 *  keeps words, occurrences(count), translations */
public class Vocabulary {
    /** Defines pairs of source-target languages which Vocabulary can work with */
    public static final LanguagePair[] SUPPORTED_LANGUAGE_PAIRS = {
            new LanguagePair("en", "ru"),
            new LanguagePair("de", "ru")
    };

    private static final Logger LOGGER = LogManager.getLogger();

    private final String source_language;
    private final String target_language;
    ArrayList<String> words = new ArrayList<>();
    private Map<String, Integer> words_occurrences = new HashMap<>();
    private Map<String, Set<String>> words_translations = new HashMap<>();

    /** Constructs Vocabulary with specified source and target languages
     *
     * @param sl source language
     * @param tl target language
     * @throws IllegalArgumentException throws exception if language is not supported or null
     */
    public Vocabulary(String sl, String tl) {
        //noinspection AccessStaticViaInstance
        if(!this.hasLanguagePair(sl, tl))
            throw new IllegalStateException("Invalid language pair");

        source_language = sl;
        target_language = tl;
    }

    /** Constructs Vocabulary with specified languages and imports entries from file
     *
     * @param sl source language
     * @param tl target language
     * @param file file to import words from
     */
    public Vocabulary(String sl, String tl, Path file) {
        this(sl, tl);

        try {
            importFromFile(file);
        }
        catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }


    /** Puts new vocabulary entry in Vocabulary
     *
     * @param word word translated
     * @param translations word's translations
     * @param occurrences word occurrences (count)
     * @return if entry was successfully added
     */
    public boolean put(String word, Set<String> translations, int occurrences) {
        return addEntry(word, translations, occurrences);
    }

    /** Adds new entry to Vocabulary
     *
     * @param word word to add
     * @param translations word's translations to add
     * @param occurrences word's occurrences to add
     */
    public boolean addEntry(String word, Set<String> translations, int occurrences) {
        Objects.requireNonNull(translations, "Set of translations must be non null");

        if(this.words.contains(word)){
            LOGGER.warn("Entry is already present");
            return false;
        }
        if(translations.isEmpty()){
            LOGGER.warn("Translations Set is empty");
            return false;
        }
        if(occurrences <= 0){
            LOGGER.warn("Occurrences must be positive number");
            return false;
        }

        this.words.add(word);
        this.words_translations.put(word, new HashSet<String>(translations));
        this.words_occurrences.put(word, occurrences);
        LOGGER.debug("New entry [" +  entryToString(word) + "] added");
        return true;
    }

    public boolean addEntry(String word, String translation, int occurrences) {
        Objects.requireNonNull(translation, "Translation must be not mull");

        if(this.words.contains(word)){
            LOGGER.warn("Entry is already present");
            return false;
        }
        if(translation.equals("")){
            LOGGER.warn("Translation is empty string");
            return false;
        }
        if(occurrences <= 0){
            LOGGER.warn("Occurrences must be positive number");
            return false;
        }

        this.words.add(word);
        this.words_translations.put(word, new HashSet<String>(Set.of(translation)));
        this.words_occurrences.put(word, occurrences);
        return true;
    }

    /** Appends existing entry
     *
     * @param key key is a word which is already in the list
     * @param translations additional translations to append
     * @param occurrences additional occurrences to append
     */
    public boolean appendEntry(String key, Set<String> translations, int occurrences) {
        Objects.requireNonNull(key, "Key must be non null");

        if(!this.words.contains(key)) {
            LOGGER.warn("No such entry key in vocabulary '" + key + "'");
            return false;
        }
        if(occurrences < 0) {
            LOGGER.warn("Negative occurrence count provided");
            return false;
        }
        if(null == translations) {
            LOGGER.warn("Null reference for translations will be replaced with empty set");
            translations = Set.of();
        }

        boolean isEmptyTranslations = this.words_translations.get(key).addAll(translations);
        boolean isZeroOccurrences = (0 == occurrences);

        if(isEmptyTranslations) {
            LOGGER.warn("No translations provided");
        }

        if(isZeroOccurrences) {
            LOGGER.warn("Occurrences count is zero");
        }
        else{
            this.words_occurrences.put(key, this.words_occurrences.get(key) + occurrences);
        }

        if(isEmptyTranslations && isZeroOccurrences) {
            LOGGER.warn("Neutral arguments provided (empty translation set and zero occurrences)");
            return false;
        }

        return true;
    }

    /** Adds translation to existing entry */
    public boolean addTranslation(String key, String translation) {
        Objects.requireNonNull(key, "Key must be non null");

        if(!this.words.contains(key)) {
            LOGGER.warn("Word list has no such key '" + key + "'");
            return false;
        }
        if(null == translation || "".equals(translation)) {
            LOGGER.warn("Translation is not provided (null or empty)");
            return false;
        }
        if(!this.words_translations.get(key).add(translation)) {
            LOGGER.warn("Translation is already present for this word '" + key + "'");
            return false;
        }

        return true;
    }

    /** Adds List of new translations to existing list */
    public boolean addTranslations(String word, Set<String> translations) {
        if(!this.words.contains(word)) {
            LOGGER.warn("No such key in vocabulary");
            return false;
        }
        if(this.words_translations.get(word).containsAll(translations)) {
            LOGGER.warn("Provided translations are already included");
            return false;
        }

        boolean anyAdding = this.words_translations.get(word).addAll(translations);
        if(!anyAdding)
            LOGGER.warn("Provided translations are already included");

        return anyAdding;
    }

    /** Removes entry from the Vocabulary */
    private boolean removeEntry(String word) {
        if(!this.words.contains(word)) {
            LOGGER.warn("No such word '" + word + "' in vocabulary");
            return false;
        }

        this.words.remove(word);
        this.words_translations.remove(word);
        this.words_occurrences.remove(word);

        return true;
    }

    /** Removes translation from list */
    private boolean removeTranslation(String word, String translation) {
        Objects.requireNonNull(word, "Key for translations removal must be non null");
        Objects.requireNonNull(translation, "Translation for removal must be non null");

        if(null == translation || "".equals(translation)) {
            LOGGER.warn("Provided translation is empty, '" + translation + "'");
            return false;
        }

        if(!this.words_translations.get(word).remove(translation)) {
            LOGGER.warn("No such translation (" + translation + ") for removal in entry ("
                    + entryToString(word) + ")");
            return false;
        }

        return true;
    }

    /** Removes provided translations for specified entry */
    private boolean removeTranslations(String key, Set<String> translations) {
        Objects.requireNonNull(key, "Key for translations removal must be non null");

        if(null == translations || translations.isEmpty()) {
            LOGGER.warn("Provided translations is empty or null, '" + translations + "'");
            return false;
        }

        if(!this.words_translations.get(key).removeAll(translations)) {
            LOGGER.warn("No such translations (" + translations + ") to remove in specified entry ("
                    + entryToString(key) +")");
            return false;
        }
        else {
            return true;
        }
    }

    /** Decreases occurrences count */
    public boolean decreaseOccurrences(String key, int occ_decrease) {
        if(occ_decrease <= 0) {
            LOGGER.warn("Occurrences decrease value is below 0, no changes will be performed");
            return false;
        }

        this.words_occurrences.put(key, this.words_occurrences.get(key) - occ_decrease);
        return true;
    }

    /** Appends fields(word + occurrences + translations) from file
     *
     * @param file Vocabulary file
     */
    public void append(Path file) {
        Objects.requireNonNull(file, "Provided Path must be non null");
        append(new Vocabulary(this.source_language, this.target_language, file));
    }

    /** Appends fields(word + occurrences + translations) from another Vocabulary
     *
     * @param another_vocabulary another Vocabulary
     */
    public void append(Vocabulary another_vocabulary) {
        Objects.requireNonNull(another_vocabulary, "Provided Path must be non null");

        if(!isVocabulariesMatchLanguages(this, another_vocabulary))
            throw new IllegalArgumentException("Vocabularies' languages don't match");

        List<String> av_words = another_vocabulary.getWordsView();
        Map<String, Integer> avw_occurrences = another_vocabulary.getWordsOccurrencesView();
        Map<String, Set<String>> avw_translations = another_vocabulary
                .getWordsTranslationsView();

        av_words.forEach(avw -> {
            if(!this.words.contains(avw))
                addEntry(avw, avw_translations.get(avw), avw_occurrences.get(avw));
            else
                appendEntry(avw, avw_translations.get(avw), avw_occurrences.get(avw));
        });
    }

    /** Imports Vocabulary from file
     *
     * @param file file path where vocabulary entries are written
     * @throws IOException throws IOException if there is problem with file
     */
    public void importFromFile(Path file) throws IOException {
        Stream<String> valid_lines;

        try{
            Pattern vocab_line_pattern = Pattern.compile("\\w+\t\\w+(\\|\\w+)*\t\\d+");
            valid_lines = Files.readAllLines(file).stream()
                    .filter(vocab_line_pattern.asMatchPredicate());

            var t_words = new ArrayList<String>();
            var t_word_occurrences = new HashMap<String, Integer>();
            var t_word_translations = new HashMap<String, Set<String>>();

            valid_lines.forEach(line -> {
                Set<String> translations;
                String[] fields = line.split("\t");

                t_words.add(fields[0]);
                translations = Set.copyOf(Arrays.asList(fields[1].split("\\|")));
                t_word_translations.put(fields[0], translations);
                t_word_occurrences.put(fields[0], Integer.valueOf(fields[2]));
            });

            words = t_words;
            words_translations = t_word_translations;
            words_occurrences = t_word_occurrences;
        }
        catch(IOException ioe){
            ioe.printStackTrace();
        }
    }

    /** Export Vocabulary fields to file
     *
     * @param export_file path of file to export to
     */
    public boolean export(Path export_file) {
        try {
            if(!Files.exists(export_file))
                Files.createFile(export_file);

            try(var writer = new PrintWriter(Files.newBufferedWriter(export_file))) {
                printVocabulary(writer);
                return true;
            }
        }
        catch(IOException ioe) {
            LOGGER.error("Failed to export to file " + export_file, ioe);
            return false;
        }
    }

    /** Returns copy of ArrayList of words */
    public ArrayList<String> cloneWords() {
        return new ArrayList<>(words);
    }

    /** Returns copy of translation entries */
    public HashMap<String, Set<String>> cloneWordsTranslations() {
        return new HashMap<>(words_translations);
    }

    /** Returns copy of Map of words occurrences */
    public Map<String, Integer> cloneWordsOccurrences() {
        return new HashMap<>(words_occurrences);
    }

    /** Returns source language */
    public String getSourceLanguage() {
        return source_language;
    }

    /** Returns target language */
    public String getTargetLanguage() {
        return target_language;
    }

    /** Returns unmodifiableList of words */
    public List<String> getWordsView() {
        return Collections.unmodifiableList(words);
    }

    /** Returns unmodifiableMap of words' occurrences */
    public Map<String, Integer> getWordsOccurrencesView() {
        return Collections.unmodifiableMap(words_occurrences);
    }

    /** Returns unmodifiableMap of words' translations */
    public Map<String, Set<String>> getWordsTranslationsView() {
        return Collections.unmodifiableMap(words_translations);
    }

    /** Prints all vocabulary fields (word + translations + occurrences) to stdout,
     * short for printVocabulary() */
    public void print() {
        printVocabulary();
    }

    /** Prints all vocabulary fields (word + translations + occurrences) to stdout */
    public void printVocabulary() {
        printVocabulary(new PrintWriter(System.out));
    }

    // CHECK(is PrintWriter a good argument)
    /** Prints all Vocabulary fields with specified PrintWriter
     *
     * @param writer specified PrintWriter which holds output stream
     */
    public void printVocabulary(PrintWriter writer) {
        words.stream().sequential().forEach(w -> {
            writer.println(entryToString(w));
            writer.flush();
        });
    }

    /** Prints all words, one per line, to stdout */
    public void printWords() {
        words.forEach(System.out::println);
    }

    /** Prints all words with its' occurrences to stdout, one entry per line */
    public void printOccurrences() {
        words.stream().sequential().forEach(
                w -> System.out.println(w + "\t" + words_occurrences.get(w))
        );
    }

    /** Prints all words and occurrence fields to stdout */
    public void printTranslations() {
        words.stream().sequential().forEach(w ->
            System.out.println(w
                    + "\t"
                    + String.join("|", words_translations.get(w)))
        );
    }

    /** Prints supported language pairs from SUPPORTED_LANGUAGE_PAIRS
     * @see me.raven.grevoc.core.Vocabulary#SUPPORTED_LANGUAGE_PAIRS
     */
    public static void printSupportedLanguagePairs() {
        for(LanguagePair pair: SUPPORTED_LANGUAGE_PAIRS) {
            String sl = pair.getSource();
            String tl = pair.getTarget();
            System.out.println(new StringBuilder().append(sl).append('-').append(tl));
        }
    }

    /** Compares source and target languages of Vocabularies
     *
     * @param v1 Vocabulary1
     * @param v2 Vocabulary2
     * @return true if languages matches
     */
    public static boolean isVocabulariesMatchLanguages(Vocabulary v1, Vocabulary v2) {
        if(null == v1)
            throw new IllegalArgumentException("Vocabulary 1 must not be null");
        if(null == v2)
            throw new IllegalArgumentException("Vocabulary 2 must not be null");

        return (v1.source_language.equals(v2.source_language)
                && v1.target_language.equals(v2.target_language));
    }

    /** Returns ArrayList of languages which are pairs to specified source language
     *
     * @param sl source language
     * @return ArrayList of target languages which are pairs, empty if no pair
     */
    public static ArrayList<String> getLanguagePairsToSource(String sl) {
        if(null == sl)
            throw new IllegalArgumentException("Source language must not be null");

        var list = new ArrayList<String>();

        for(LanguagePair pair: SUPPORTED_LANGUAGE_PAIRS)
            if(pair.getSource().equals(sl))
                list.add(pair.getTarget());

        return list;
    }

    /** Returns ArrayList of languages which are pairs to specified target language
     *
     * @param tl target language
     * @return ArrayList of source languages which are pairs, empty if no pair for specified target
     */
    public static ArrayList<String> getLanguagePairsToTarget(String tl) {
        if(null == tl)
            throw new IllegalArgumentException("Target language must not be null");

        var list = new ArrayList<String>();

        for(LanguagePair pair: SUPPORTED_LANGUAGE_PAIRS)
            if(pair.getTarget().equals(tl))
                list.add(pair.getSource());

        return list;
    }

    /** Checks if specified language is supported as source
     *
     * @param language specified source language
     * @return true if language presents in at least one language pair as source, otherwise false
     */
    public static boolean hasSourceLanguage(String language) {
        if(null == language)
            throw new IllegalArgumentException("Language must not be null");

        for(LanguagePair pair: SUPPORTED_LANGUAGE_PAIRS)
            if(pair.getSource().equals(language))
                return true;

        return false;
    }

    /** Checks if specified language is supported as source
     *
     * @param language specified target language
     * @return true if language presents in at least one language pair as target, otherwise false
     */
    public static boolean hasTargetLanguage(String language) {
        if(null == language)
            throw new IllegalArgumentException("Language must not be null");

        for(LanguagePair pair: SUPPORTED_LANGUAGE_PAIRS)
            if(pair.getTarget().equals(language))
                return true;

        return false;
    }

    /** Checks if there is a pair of specified languages in SUPPORTED_LANGUAGE_PAIRS
     *
     * @param sl source language
     * @param tl target language
     * @return true if pair exists, false otherwise
     * @see me.raven.grevoc.core.Vocabulary#SUPPORTED_LANGUAGE_PAIRS
     */
    public static boolean hasLanguagePair(String sl, String tl) {
        if(null == sl)
            throw new IllegalArgumentException("Source language must not be null");
        if(null == tl)
            throw new IllegalArgumentException("Target language must not be null");

        for(LanguagePair pair: SUPPORTED_LANGUAGE_PAIRS)
            if(pair.getSource().equals(sl) && pair.getTarget().equals(tl))
                return true;

        return false;
    }

    private String entryToString(String word) {
        if(this.words.contains(word)) {
            LOGGER.warn("No such entry key '" + word + "'");
            return "";
        }

        var entry_string_joiner = new StringJoiner("|");
        words_translations.get(word).forEach(entry_string_joiner::add);
        return word + "\t" + entry_string_joiner.toString() + "\t" + words_occurrences.get(word);
    }
}