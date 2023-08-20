package me.raven.grevoc.core.translator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TranslatorTest {
    @Test
    void visualTestSetterReturnObject() {
        Translator debug = new Debug("en", "ru");
        Translator debug_holder;
        Debug debug_obj = new Debug("en", "ru");

        System.out.println(debug);
        System.out.println(debug_holder = debug);
        System.out.println(debug_obj);
    }
}