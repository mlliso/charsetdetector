/**
 * MIT License
 * <p>
 * Copyright (c) 2018 Michał Lisiecki
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package pl.mlisiecki.charsetdetector;

import pl.mlisiecki.charsetdetector.domain.ImmutablePair;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CharsetDetectorImpl implements CharsetDetector {

    private static final String PATTERN_TEMPLATE = "[%s]";
    private static final Locale plPl = Locale.forLanguageTag("pl_PL");


    private final Map<Locale, Charset[]> charsetsByLocale;
    private final Map<Locale, Pattern> patternsByLocale;
    private final Charset defaultCharset;

    /**
     * Creates detector for selected locale
     *
     * @param defaultCharset charset to return when charset is not recognized
     */
    public CharsetDetectorImpl(Charset defaultCharset) {
        this.charsetsByLocale = new ConcurrentHashMap<>();
        this.charsetsByLocale.put(
                plPl,
                new Charset[]{StandardCharsets.UTF_8, Charset.forName("ISO-8859-2"), Charset.forName("WINDOWS-1250")}
        );
        this.patternsByLocale = new HashMap<>();
        this.patternsByLocale.put(
                plPl,
                Pattern.compile("[\u0105\u0107\u0119\u0142\u0144\u00F3\u015B\u017A\u017C\u0104\u0106\u0118\u0141\u0143\u00D3\u015A\u0179\u017B]")
        );
        this.defaultCharset = defaultCharset;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Charset detectCharset(Locale locale, byte[] bytesContent) {
        if (!(charsetsByLocale.containsKey(locale) && patternsByLocale.containsKey(locale))) {
            throw new IllegalArgumentException(String.format("Locale %s is not registered", locale));
        }
        final ByteBuffer bytesContentBuffer = ByteBuffer.wrap(bytesContent);

        return Arrays.stream(charsetsByLocale.get(locale))
                .map(charset -> new ImmutablePair<>(charset, countDiacritics(locale, bytesContentBuffer, charset.newDecoder())))
                .max(Comparator.comparing(c -> c.right))
                .filter(e -> e.right > 0)
                .map(ImmutablePair::getLeft)
                .orElse(defaultCharset);
    }

    private int countDiacritics(final Locale locale, ByteBuffer bytes, CharsetDecoder decoder) {
        String x = "κείμενο\u0000";
        try {
            final String text = decoder.decode(bytes).toString();
            final Matcher matcher = patternsByLocale.get(locale).matcher(text);
            int from = 0;
            int count = 0;
            while (matcher.find(from)) {
                count++;
                from = matcher.start() + 1;
            }
            return count;
        } catch (CharacterCodingException e) {
            return 0;
        } finally {
            bytes.rewind();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addDiacriticsCharactersForLocale(final Locale locale, final String diacritics) {
        patternsByLocale.put(locale, Pattern.compile(String.format(PATTERN_TEMPLATE, diacritics)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addCharsetsToTestForLocale(final Locale locale, final Charset[] charsets) {
        charsetsByLocale.put(locale, charsets);
    }
}

