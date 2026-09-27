package org.takoyaki.curriculummanager.util;

import org.takoyaki.curriculummanager.i18n.I18n;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class JsonUtils {
    private JsonUtils() {
    }

    public static Object parse(String source) {
        Parser parser = new Parser(source);
        Object value = parser.readValue();
        parser.skipWhitespace();

        if (!parser.isFinished()) {
            throw parser.error(I18n.raw("json.error.trailing"));
        }

        return value;
    }

    public static String write(Object value) {
        StringBuilder builder = new StringBuilder();
        append(builder, value, 0);
        builder.append('\n');
        return builder.toString();
    }

    private static void append(StringBuilder builder, Object value, int depth) {
        if (value == null) {
            builder.append("null");
        } else if (value instanceof String string) {
            appendString(builder, string);
        } else if (value instanceof Number || value instanceof Boolean) {
            builder.append(value);
        } else if (value instanceof Map<?, ?> map) {
            appendObject(builder, map, depth);
        } else if (value instanceof List<?> list) {
            appendArray(builder, list, depth);
        } else if (value instanceof byte[] bytes) {
            appendString(builder, java.util.Base64.getEncoder().encodeToString(bytes));
        } else {
            appendString(builder, value.toString());
        }
    }

    private static void appendObject(StringBuilder builder, Map<?, ?> map, int depth) {
        builder.append('{');

        if (!map.isEmpty()) {
            builder.append('\n');
            int itemIndex = 0;

            for (Map.Entry<?, ?> entry : map.entrySet()) {
                indent(builder, depth + 1);
                appendString(builder, String.valueOf(entry.getKey()));
                builder.append(": ");
                append(builder, entry.getValue(), depth + 1);

                if (++itemIndex < map.size()) {
                    builder.append(',');
                }

                builder.append('\n');
            }

            indent(builder, depth);
        }

        builder.append('}');
    }

    private static void appendArray(StringBuilder builder, List<?> list, int depth) {
        builder.append('[');

        if (!list.isEmpty()) {
            builder.append('\n');

            for (int itemIndex = 0; itemIndex < list.size(); itemIndex++) {
                indent(builder, depth + 1);
                append(builder, list.get(itemIndex), depth + 1);

                if (itemIndex + 1 < list.size()) {
                    builder.append(',');
                }

                builder.append('\n');
            }

            indent(builder, depth);
        }

        builder.append(']');
    }

    private static void appendString(StringBuilder builder, String value) {
        builder.append('"');

        for (int characterIndex = 0; characterIndex < value.length(); characterIndex++) {
            char character = value.charAt(characterIndex);

            switch (character) {
                case '"' -> builder.append("\\\"");
                case '\\' -> builder.append("\\\\");
                case '\b' -> builder.append("\\b");
                case '\f' -> builder.append("\\f");
                case '\n' -> builder.append("\\n");
                case '\r' -> builder.append("\\r");
                case '\t' -> builder.append("\\t");
                default -> {
                    if (character < 0x20) {
                        builder.append(String.format("\\u%04x", (int) character));
                    } else {
                        builder.append(character);
                    }
                }
            }
        }

        builder.append('"');
    }

    private static void indent(StringBuilder builder, int depth) {
        builder.append("  ".repeat(depth));
    }

    private static final class Parser {
        private final String source;
        private int index;

        private Parser(String source) {
            if (source == null) {
                throw new IllegalArgumentException(I18n.raw("json.error.required"));
            }

            this.source = source;
        }

        private Object readValue() {
            skipWhitespace();

            if (isFinished()) {
                throw error(I18n.raw("json.error.unexpectedEnd"));
            }

            return switch (source.charAt(index)) {
                case '{' -> readObject();
                case '[' -> readArray();
                case '"' -> readString();
                case 't' -> readLiteral("true", Boolean.TRUE);
                case 'f' -> readLiteral("false", Boolean.FALSE);
                case 'n' -> readLiteral("null", null);
                default -> readNumber();
            };
        }

        private Map<String, Object> readObject() {
            expect('{');
            Map<String, Object> values = new LinkedHashMap<>();
            skipWhitespace();

            if (consume('}')) {
                return values;
            }

            while (true) {
                skipWhitespace();
                String key = readString();
                skipWhitespace();
                expect(':');
                values.put(key, readValue());
                skipWhitespace();

                if (consume('}')) {
                    return values;
                }

                expect(',');
            }
        }

        private List<Object> readArray() {
            expect('[');
            List<Object> values = new ArrayList<>();
            skipWhitespace();

            if (consume(']')) {
                return values;
            }

            while (true) {
                values.add(readValue());
                skipWhitespace();

                if (consume(']')) {
                    return values;
                }

                expect(',');
            }
        }

        private String readString() {
            expect('"');
            StringBuilder builder = new StringBuilder();

            while (!isFinished()) {
                char character = source.charAt(index++);

                if (character == '"') {
                    return builder.toString();
                }

                if (character < 0x20) {
                    throw error(I18n.raw("json.error.controlCharacter"));
                }

                if (character != '\\') {
                    builder.append(character);
                    continue;
                }

                if (isFinished()) {
                    throw error(I18n.raw("json.error.escape"));
                }

                char escaped = source.charAt(index++);

                switch (escaped) {
                    case '"', '\\', '/' -> builder.append(escaped);
                    case 'b' -> builder.append('\b');
                    case 'f' -> builder.append('\f');
                    case 'n' -> builder.append('\n');
                    case 'r' -> builder.append('\r');
                    case 't' -> builder.append('\t');
                    case 'u' -> builder.append(readUnicode());
                    default -> throw error(I18n.raw("json.error.escape"));
                }
            }

            throw error(I18n.raw("json.error.unclosedString"));
        }

        private char readUnicode() {
            if (index + 4 > source.length()) {
                throw error(I18n.raw("json.error.unicodeEscape"));
            }

            String hexadecimal = source.substring(index, index + 4);
            index += 4;

            try {
                return (char) Integer.parseInt(hexadecimal, 16);
            } catch (NumberFormatException e) {
                throw error(I18n.raw("json.error.unicodeEscape"));
            }
        }

        private Object readNumber() {
            int start = index;

            if (consume('-') && isFinished()) {
                throw error(I18n.raw("json.error.number"));
            }

            if (consume('0')) {
                if (!isFinished() && Character.isDigit(source.charAt(index))) {
                    throw error(I18n.raw("json.error.number"));
                }
            } else {
                readDigits();
            }

            boolean decimal = false;

            if (consume('.')) {
                decimal = true;
                readDigits();
            }

            if (!isFinished() && (source.charAt(index) == 'e' || source.charAt(index) == 'E')) {
                decimal = true;
                index++;

                if (!isFinished() && (source.charAt(index) == '+' || source.charAt(index) == '-')) {
                    index++;
                }

                readDigits();
            }

            String number = source.substring(start, index);

            try {
                if (decimal) {
                    return Double.valueOf(number);
                }

                return Long.valueOf(number);
            } catch (NumberFormatException e) {
                throw error(I18n.raw("json.error.number"));
            }
        }

        private void readDigits() {
            int start = index;

            while (!isFinished() && Character.isDigit(source.charAt(index))) {
                index++;
            }

            if (start == index) {
                throw error(I18n.raw("json.error.number"));
            }
        }

        private Object readLiteral(String literal, Object value) {
            if (!source.startsWith(literal, index)) {
                throw error(I18n.raw("json.error.value"));
            }

            index += literal.length();
            return value;
        }

        private void expect(char expected) {
            if (!consume(expected)) {
                throw error(I18n.raw("json.error.expected", expected));
            }
        }

        private boolean consume(char expected) {
            if (!isFinished() && source.charAt(index) == expected) {
                index++;
                return true;
            }

            return false;
        }

        private void skipWhitespace() {
            while (!isFinished() && Character.isWhitespace(source.charAt(index))) {
                index++;
            }
        }

        private boolean isFinished() {
            return index >= source.length();
        }

        private IllegalArgumentException error(String message) {
            return new IllegalArgumentException(I18n.raw("json.error.position", message, index));
        }
    }
}
