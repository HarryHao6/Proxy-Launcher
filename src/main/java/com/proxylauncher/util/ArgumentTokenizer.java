package com.proxylauncher.util;

import java.util.ArrayList;
import java.util.List;

public final class ArgumentTokenizer {
    private ArgumentTokenizer() {
    }

    public static List<String> tokenize(String rawArguments) {
        if (rawArguments == null || rawArguments.isBlank()) {
            return List.of();
        }

        List<String> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        boolean inQuotes = false;
        char quoteChar = 0;

        for (int index = 0; index < rawArguments.length(); index++) {
            char current = rawArguments.charAt(index);

            if (inQuotes) {
                if (current == quoteChar) {
                    inQuotes = false;
                } else {
                    currentToken.append(current);
                }
                continue;
            }

            if (current == '"' || current == '\'') {
                inQuotes = true;
                quoteChar = current;
                continue;
            }

            if (Character.isWhitespace(current)) {
                flushToken(tokens, currentToken);
                continue;
            }

            currentToken.append(current);
        }

        flushToken(tokens, currentToken);
        return tokens;
    }

    private static void flushToken(List<String> tokens, StringBuilder currentToken) {
        if (currentToken.length() == 0) {
            return;
        }
        tokens.add(currentToken.toString());
        currentToken.setLength(0);
    }
}
