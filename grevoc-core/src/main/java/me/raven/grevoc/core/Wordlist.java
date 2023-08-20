package me.raven.grevoc.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


/** Simple word list which counts repetitions */
public class Wordlist {
    private ArrayList<String> words;
    private Map<String, Integer> words_occurrences;

    /** Constructs empty WordList */
    public Wordlist() {
        words = new ArrayList<>();
        words_occurrences = new HashMap<>();
    }

    /** Constructs WordList with words from specified file
     *
     * @param filename Path of the file
     */
    public Wordlist(Path filename) {
        this();
        append(filename);
   }


    public void importFromFile(Path filename) {
        clear();
        append(filename);
    }

    /** Appends WordList with words from file
     *
     * @param filename Path to file
     */
    public void append(Path filename) {
        append(filename, words, words_occurrences);
    }

    /** Appends wordlist and words' occurrences with words from file
     *
     * @param filename Path to file
     * @param w wordlist
     * @param wo word occurrences
     */
    public void append(Path filename, ArrayList<String> w, Map<String, Integer> wo) {
         try {
             List<String> new_words = Files.readAllLines(filename);

             new_words.forEach(nw -> {
                 if(w.contains(nw))
                     wo.put(nw, wo.get(nw) + 1);
                 else{
                     w.add(nw);
                     wo.put(nw, 1);
                 }
             });
        }
        catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /** Sorts words in descending alphabetic order */
    public void sort() {
        Collections.sort(words);
    }

    /** Clears WordList  form words and occurrences */
    public void clear() {
        words.clear();
        words_occurrences.clear();
    }

    public List getWords() {
        return new ArrayList(words);
    }
    public Map getWordsOccurrences() {
        return new HashMap(words_occurrences);
    }

    /** Gets view of words as immutable List */
    public List getWordsView() {
        return Collections.unmodifiableList(words);
    }


    /** Gets view of words occurrences as immutable Map */
    public Map getWordsOccurrencesView() {
        return Collections.unmodifiableMap(words_occurrences);
    }


    /** Prints words with number of occurrences list to standard output */
    public void print() {
        printWords();
    }


    /** Prints words with number of occurrences list to standard output */
    public void printWords() {
        words.forEach(word -> {
            System.out.println(word + "\t" + words_occurrences.get(word));
        });
    }
}