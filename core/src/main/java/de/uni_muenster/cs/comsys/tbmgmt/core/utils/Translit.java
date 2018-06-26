package de.uni_muenster.cs.comsys.tbmgmt.core.utils;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

/**
 Based upon http://stackoverflow.com/a/22841035/1266906
 */
public class Translit {
    public static final HashMap<Integer, String> REPLACEMENTS = new ReplacementBuilder().put('„', '"')
            .put('“', '"')
            .put('”', '"')
            .put('¨', '"')
            .put('″', '"')
            .put('€', "EUR")
            .put('ß', "ss")
            .put('•', '*')
            .put('Œ', "OE")
            .put('œ', "oe")
            .put('Æ', "AE")
            .put('æ', "ae")
            .put('Ð', "D")
            .put('ð', "d")
            .put('×', "*")
            .put('÷', "/")
            .put('Ø', "O")
            .put('ø', "o")
            .put('Þ', "TH")
            .put('þ', "th")
            .put('⁄', '/') // Java uses this when decomposing e.g. ¼
            .getMap();

    public static String toCharset(final String input, Charset charset) {
        return toCharset(input, charset, Translit.REPLACEMENTS);
    }

    public static String toCharset(final String input,
            Charset charset,
            Map<? super Integer, ? extends String> replacements) {
        final CharsetEncoder charsetEncoder = charset.newEncoder();
        return toCharset(input, charsetEncoder, replacements);
    }

    private static String toCharset(String input,
            CharsetEncoder charsetEncoder,
            Map<? super Integer, ? extends String> replacements) {
        char[] data = input.toCharArray();
        final StringBuilder sb = new StringBuilder(data.length);

        input.codePoints().forEachOrdered(codePoint -> {
            final char[] chars = Character.toChars(codePoint);
            final CharBuffer charBuffer = CharBuffer.wrap(chars);
            if (charsetEncoder.canEncode(charBuffer)) {
                sb.append(chars);
            } else if (replacements.containsKey(codePoint)) {
                sb.append(toCharset(replacements.get(codePoint), charsetEncoder, replacements));
            } else {
                // Only perform NFKD Normalization after ensuring the original character is invalid as this is a
                // irreversible process
                Normalizer.normalize(charBuffer, Normalizer.Form.NFKD)
                        .codePoints()
                        .forEachOrdered(decomposedCodePoint -> {
                            final char[] decomposedChars = Character.toChars(decomposedCodePoint);

                            if (charsetEncoder.canEncode(CharBuffer.wrap(decomposedChars))) {
                                sb.append(decomposedChars);
                            } else if (replacements.containsKey(decomposedCodePoint)) {
                                sb.append(
                                        toCharset(replacements.get(decomposedCodePoint), charsetEncoder, replacements));
                            }
                            // else: drop codePoint foreign to charset
                        });
            }
        });
        return sb.toString();
    }

    public static class MapBuilder<K, V> {

        private final HashMap<K, V> map;

        public MapBuilder() {
            map = new HashMap<K, V>();
        }

        public MapBuilder<K, V> put(K key, V value) {
            map.put(key, value);
            return this;
        }

        public HashMap<K, V> getMap() {
            return map;
        }
    }

    public static class ReplacementBuilder extends MapBuilder<Integer, String> {
        public ReplacementBuilder() {
            super();
        }

        public ReplacementBuilder put(Integer input, char replacement) {
            return this.put(input, String.valueOf(replacement));
        }

        @Override
        public ReplacementBuilder put(Integer input, String replacement) {
            super.put(input, replacement);
            return this;
        }

        public ReplacementBuilder put(char input, String replacement) {
            return this.put((int) input, replacement);
        }

        public ReplacementBuilder put(char input, char replacement) {
            return this.put((int) input, String.valueOf(replacement));
        }
    }
}
