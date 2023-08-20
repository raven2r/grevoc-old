package me.raven.grevoc.core.translator;

import org.junit.jupiter.api.Test;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

import static org.junit.jupiter.api.Assertions.*;

class TranslatorManagerTest {
    @Test
    public void checkEngineNameField() {
        try(ScanResult scan_result =
                    new ClassGraph()
                            .verbose()
                            .enableAllInfo()
                            .acceptPackages("me.raven.grevoc.core.translator")
                            .scan()) {
            scan_result.getSubclasses(Translator.class).forEach(cl ->
                            assertTrue(null !=
                                    cl.getFieldInfo("ENGINE_NAME")
                                    .getConstantInitializerValue(),
                                    "ENGINE_NAME is not overridden in some translator(s)")
                    );
        }
    }
}