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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author mlisiecki
 */
class CharsetDetectorImplTest {
    static final Charset ISO_8859_2 = Charset.forName("ISO-8859-2");
    static final Charset WINDOWS_1250 = Charset.forName("WINDOWS-1250");
    static final Charset ISO_8859_7 = Charset.forName("ISO-8859-7");


    static Stream<Arguments> shouldDetectPolishCharsetTestCases() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(ISO_8859_2, ISO_8859_2.encode(plText())))
                .add(Arguments.of(WINDOWS_1250, WINDOWS_1250.encode(plText())))
                .add(Arguments.of(StandardCharsets.UTF_8, StandardCharsets.UTF_8.encode(plText())))
                .add(Arguments.of(StandardCharsets.UTF_8, StandardCharsets.UTF_16BE.encode(plText())))
                .build();
    }

    static Stream<Arguments> shouldDetectGreekCharsetTestCases() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(ISO_8859_7, ISO_8859_7.encode(grText())))
                .add(Arguments.of(StandardCharsets.UTF_8, StandardCharsets.UTF_8.encode(grText())))
                .build();
    }

    private static String grText() {
        return "κείμενο";
    }

    static String plText() {
        return "P\u0142e\u0107;\u0105\u017A\u0107\u0119\u0142";
    }

    static String greekDiacritics() {
        String a = "\u039e\u038a\u039e\u0385\u039e\u2015\u039e\u038c\u039e\u0385\u039e\u00bd\u039e\u038f";
        return "αεηιουωᾳῃῳράέήίόύώᾴῄῴὰὲὴὶὸὺὼᾲῂῲᾶῆῖῦῶᾷῇῷἀἐἠἰὀὐὠᾀᾐᾠῤἄἔἤἴὄὔὤᾄᾔᾤἂἒἢἲὂὒὢᾂᾒᾢἆἦἶὖὦᾆᾖᾦἁἑἡἱὁὑὡᾁᾑᾡῥἅἕἥἵὅὕὥᾅᾕᾥἃἓἣἳὃὓὣᾃᾓᾣἇἧἷὗὧᾇᾗᾧϊϋΐΰῒῢῗῧᾱῑῡᾰῐῠ" +
                "ΑΕΗΙΟΥΩᾼῌῼΡΆΈΉΊΌΎΏᾺῈῊῚῸῪῺἈἘἨἸὈὨᾈᾘᾨἌἜἬἼὌὬᾌᾜᾬἊἚἪἺὊὪᾊᾚᾪἎἮἾὮᾎᾞᾮἉἙἩἹὉὙὩᾉᾙᾩῬἍἝἭἽὍὝὭᾍᾝᾭἋἛἫἻὋὛὫᾋᾛᾫἏἯἿὟὯᾏᾟᾯΪΫᾹῙῩᾸῘῨ";
    }

    @ParameterizedTest
    @MethodSource("shouldDetectPolishCharsetTestCases")
    void shouldDetectPolishCharset(Charset expected, ByteBuffer bytes) {
        final CharsetDetectorImpl charsetDetectorImpl = new CharsetDetectorImpl(StandardCharsets.UTF_8);

        final Charset charset = charsetDetectorImpl.detectCharset(Locale.forLanguageTag("pl_PL"), bytes.array());

        assertEquals(expected, charset);
    }

    @ParameterizedTest
    @MethodSource("shouldDetectGreekCharsetTestCases")
    void shouldDetectGreekCharset(Charset expected, ByteBuffer bytes) {
        final Locale el_GR = Locale.forLanguageTag("el_GR");
        final CharsetDetectorImpl charsetDetectorImpl = new CharsetDetectorImpl(StandardCharsets.UTF_8);
        charsetDetectorImpl.addCharsetsToTestForLocale(el_GR, new Charset[]{StandardCharsets.UTF_8, ISO_8859_7});
        charsetDetectorImpl.addDiacriticsCharactersForLocale(el_GR, greekDiacritics());

        final Charset charset = charsetDetectorImpl.detectCharset(el_GR, bytes.array());

        assertEquals(expected, charset);
    }
}