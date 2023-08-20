package me.raven.grevoc.core;

import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;


class WordlistTest {
    @Test
    public void doubleAppendTestCompression() throws Exception {
        var wl = new Wordlist();
        wl.append(Path.of(
                    getClass()
                    .getResource("/wordlist.txt")
                    .toURI())
        );

        Map<String, Integer> first_occur_count = wl.getWordsOccurrences();

        wl.append(Path.of(
                getClass()
                        .getResource("/wordlist.txt")
                        .toURI())
        );

        Map<String, Integer> second_occur_count = wl.getWordsOccurrences();

        System.out.println(first_occur_count);
        System.out.println(second_occur_count);

        first_occur_count.entrySet().forEach(entry -> {
            assertTrue(2 * entry.getValue() == second_occur_count.get(entry.getKey()),
                    "Number of words' occurrences of the twice imported same wordlist"
                    + "hasn't been doubled.");
        });

        System.out.println(wl.toString());
    }
}