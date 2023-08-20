package me.raven.grevoc.core;

import me.raven.grevoc.core.translator.Debug;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.instrument.UnmodifiableClassException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


class VocabularyTest {
    class TestVocabulary extends Vocabulary {
        TestVocabulary(String sl, String tl) {
            super(sl, tl);
        }

        @Override
        public String toString() {
            return "";
        }
    }

    private Vocabulary vocabulary = new Vocabulary("en", "ru");

    @Test
    public void stupidVisualTest1() {
        Vocabulary wl;

        try {
            wl = new Vocabulary("en", "ru", Path.of(
                    getClass()
                    .getResource("/wordlist.txt")
                    .toURI()
            ));

            System.out.println("Initial list:");

            System.out.println("\nWord count:");
            wl.printOccurrences();

            System.out.println("\nTranslations empty:");
            wl.printTranslations();
            System.out.println("\nTranslations:");
            wl.printTranslations();

            System.out.println("\nWords:");
            wl.printWords();

            System.out.println("\nTranslations:");
            wl.printTranslations();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void stupidVisualTest2() {
        Vocabulary wl;
        try {
            wl = new Vocabulary("en", "ru", Path.of(
                    getClass()
                            .getResource("/wordlist.txt")
                            .toURI()
            ));

            wl.print();
            System.out.println();
            wl.print();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void stupidVisualTest3(){
        Vocabulary wl;
        try {
            Path wordlist_path = Path.of(getClass().getResource("/wordlist.txt").toURI());
            wl = new Vocabulary("en", "ru", wordlist_path);

            wl.append(wordlist_path);
            wl.print();
            System.out.println();

            wl.print();
            wl.append(wordlist_path);
            System.out.println();

            wl.print();
            System.out.println();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private Vocabulary makeTestVocabulary() {
        Vocabulary vocabulary = null;

        try {
             vocabulary = new Vocabulary("en", "ru", Path.of(
                    getClass()
                    .getResource("/wordlist.txt")
                    .toURI()
            ));

        }
        catch(Exception e){
            e.printStackTrace();
        }

        return vocabulary;
    }

    @Test
    public void visualVocabularyTestForAppending() {
        Vocabulary wl;
        try {
            Path test_path = Paths.get("src", "test", "resources");
            //new Path(test_path).toAbsolutePath();

            Path wordlist_path = Path.of(getClass().getResource("/wordlist.txt").toURI());
            wl = new Vocabulary("en", "ru", wordlist_path);

            wl.append(wordlist_path);
            System.out.println("Occurrences:");
            wl.printOccurrences();
            System.out.println();

            wl.print();
            System.out.println();
            wl.print();
            wl.append(wordlist_path);
            System.out.println();
            wl.print();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void viewsReturnsReallyUnmodifiableCollections() {
        var vocabulary = new Vocabulary("en", "ru");

        assertThrows(UnsupportedOperationException.class, () -> {
            vocabulary.getWordsView().add("bulk");
        });

        assertThrows(UnsupportedOperationException.class, () -> {
            vocabulary.getWordsOccurrencesView().put("bulk", -999);
        });

        assertThrows(UnsupportedOperationException.class, () -> {
            vocabulary.getWordsTranslationsView().put("bulk", Set.of("t1", "t2"));
        });
   }

   @Test
   public void clonesOfCollectionsReturnReallyNewObjects() {
       var vocabulary = new Vocabulary("en", "ru");
       var baout = new ByteArrayOutputStream(100);
       String vocabulary_print;

       // Insert field into Vocabulary and print it to string
       vocabulary.put("from", Set.of("t1", "t2", "t3"), 8);
       vocabulary.printVocabulary(new PrintWriter(baout));
       vocabulary_print = baout.toString();
       baout.reset();

       // Modify Map which might be internal object of Vocabulary
       vocabulary.cloneWordsTranslations().put("modification", Set.of("t21", "t22"));
       vocabulary.printVocabulary(new PrintWriter(baout));
       assertTrue(baout.toString().equals(vocabulary_print),
               "Vocabulary's translations have been changed");
       baout.reset();

       // Try to modify cloned words list and check if it has affect on internal list
       ArrayList<String> words = vocabulary.cloneWords();
       words.add("new1");
       words.add("new2");
       words.add("new3");
       vocabulary.printVocabulary(new PrintWriter(baout));
       assertTrue(baout.toString().equals(vocabulary_print),
               "Vocabulary's words have been changed");
       baout.reset();

       // Try to modify occurrences count inner Map
       vocabulary.cloneWordsOccurrences().put("occurrence_new", 10);
       vocabulary.printVocabulary(new PrintWriter(baout));
       assertTrue(baout.toString().equals(vocabulary_print),
               "Vocabulary's occurrences have been changed");
       baout.reset();

       // Try to insert new fields to make sure vocabulary can be modified by invoking proper method
       vocabulary.put("from1", Set.of("r1", "r2", "r3"), 3);
       vocabulary.put("from1", Set.of("r1", "r2", "r3"), 3);
       vocabulary.printVocabulary(new PrintWriter(baout));
       assertFalse(baout.toString().equals(vocabulary_print),
               "Vocabulary hasn't been changed");
       baout.reset();

       try{
           baout.close();
       }
       catch (IOException ioe) {
           ioe.printStackTrace();
       }
   }

   @Test
    public void checkAppendingMethods() {
        var vocabulary = new Vocabulary("en", "ru");
        vocabulary.put("word1", Set.of("tr1", "tr2", "tr3"), 1);
        vocabulary.addTranslations("word1", Set.of("nt1", "nt2", "nt3"));
        Set translations = vocabulary.getWordsTranslationsView().get("word1");
        assertTrue(translations.containsAll(List.of("nt1", "nt2", "nt3")),
                "Vocabulary has not not append specified translations");
   }
}