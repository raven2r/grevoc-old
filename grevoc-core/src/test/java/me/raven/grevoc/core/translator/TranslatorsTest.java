package me.raven.grevoc.core.translator;

import org.junit.jupiter.api.Test;

class TranslatorsTest {
    @Test
    void createDebugTranslator() {
        TranslatorManager.construct("debug", "en", "ru");
    }
}