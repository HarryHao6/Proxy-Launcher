package com.proxylauncher.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ArgumentTokenizerTest {
    @Test
    public void splitsQuotedAndUnquotedArguments() {
        assertEquals(
                List.of("--profile", "Proxy Test", "--verbose"),
                ArgumentTokenizer.tokenize("--profile \"Proxy Test\" --verbose")
        );
    }

    @Test
    public void ignoresBlankInput() {
        assertEquals(List.of(), ArgumentTokenizer.tokenize("   "));
    }
}
