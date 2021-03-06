/*
 * Copyright (c) 2008-2012, Stephen Colebourne & Michael Nascimento Santos
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  * Neither the name of JSR-310 nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package javax.time.format;

import static javax.time.calendrical.ChronoField.DAY_OF_MONTH;
import static javax.time.calendrical.ChronoField.DAY_OF_WEEK;
import static javax.time.calendrical.ChronoField.DAY_OF_YEAR;
import static javax.time.calendrical.ChronoField.HOUR_OF_DAY;
import static javax.time.calendrical.ChronoField.MINUTE_OF_HOUR;
import static javax.time.calendrical.ChronoField.MONTH_OF_YEAR;
import static javax.time.calendrical.ChronoField.NANO_OF_SECOND;
import static javax.time.calendrical.ChronoField.SECOND_OF_MINUTE;
import static javax.time.calendrical.ChronoField.YEAR;

import java.util.Locale;
import java.util.Objects;

import javax.time.calendrical.DateTimeField;
import javax.time.calendrical.ISOWeeks;

/**
 * Provides common implementations of {@code DateTimeFormatter}.
 * <p>
 * This utility class provides three different ways to obtain a formatter.
 * <p><ul>
 * <li>Using pattern letters, such as {@code yyyy-MMM-dd}
 * <li>Using localized styles, such as {@code long} or {@code medium}
 * <li>Using predefined constants, such as {@code isoLocalDate()}
 * </ul><p>
 *
 * <h4>Implementation notes</h4>
 * This is a thread-safe utility class.
 * All returned formatters are immutable and thread-safe.
 */
public final class DateTimeFormatters {

    /**
     * Private constructor since this is a utility class.
     */
    private DateTimeFormatters() {
    }

    //-----------------------------------------------------------------------
    /**
     * Creates a formatter using the specified pattern.
     * <p>
     * This method will create a formatter based on a simple pattern of letters and symbols.
     * For example, {@code d MMM yyyy} will format 2011-12-03 as '3 Dec 2011'.
     * <p>
     * The returned formatter will use the default locale, but this can be changed
     * using {@link DateTimeFormatter#withLocale(Locale)}.
     * <p>
     * All letters 'A' to 'Z' and 'a' to 'z' are reserved as pattern letters.
     * The following pattern letters are defined:
     * <pre>
     *  Symbol  Meaning                     Presentation      Examples
     *  ------  -------                     ------------      -------
     *   G       era                         number/text       1; 01; AD; Anno Domini
     *   y       year                        year              2004; 04
     *   D       day-of-year                 number            189
     *   M       month-of-year               number/text       7; 07; Jul; July; J
     *   d       day-of-month                number            10
     *
     *   Q       quarter-of-year             number/text       3; 03; Q3
     *   Y       week-based-year             year              1996; 96
     *   w       week-of-week-based-year     number            27
     *   E       day-of-week                 number/text       2; Tue; Tuesday; T
     *   F       week-of-month               number            3
     *
     *   a       am-pm-of-day                text              PM
     *   h       clock-hour-of-am-pm (1-12)  number            12
     *   K       hour-of-am-pm (0-11)        number/fraction   0
     *   k       clock-hour-of-am-pm (1-24)  number            0
     *
     *   H       hour-of-day (0-23)          number/fraction   0
     *   m       minute-of-hour              number/fraction   30
     *   s       second-of-minute            number/fraction   55
     *   S       milli-of-second             number/fraction   978
     *   A       milli-of-day                number/fraction   1234
     *   n       nano-of-second              number/fraction   987654321
     *   N       nano-of-day                 number/fraction   1234000000
     *
     *   I       time-zone ID                zoneId            America/Los_Angeles
     *   z       time-zone name              text              Pacific Standard Time; PST
     *   Z       zone-offset                 offset-Z          +0000; -0800; -08:00;
     *   X       zone-offset 'Z' for zero    offset-X          Z; -0800; -08:00;
     *
     *   f       make next a fraction        fraction modifier .123
     *   p       pad next                    pad modifier      1
     *
     *   '       escape for text             delimiter
     *   ''      single quote                literal           '
     *   [       optional section start
     *   ]       optional section end
     * </pre>
     * <p>
     * The count of pattern letters determine the format.
     * <p>
     * <b>Text</b>: The text style is determined based on the number of pattern letters used.
     * Less than 4 pattern letters will use the {@link TextStyle#SHORT short form}.
     * Exactly 4 pattern letters will use the {@link TextStyle#FULL full form}.
     * Exactly 5 pattern letters will use the {@link TextStyle#NARROW narrow form}.
     * <p>
     * <b>Number</b>: If the count of letters is one, then the value is printed using the minimum number
     * of digits and without padding as per {@link DateTimeFormatterBuilder#appendValue(DateTimeField)}.
     * Otherwise, the count of digits is used as the width of the output field as per
     * {@link DateTimeFormatterBuilder#appendValue(DateTimeField, int)}.
     * <p>
     * <b>Number/Text</b>: If the count of pattern letters is 3 or greater, use the Text rules above.
     * Otherwise use the Number rules above.
     * <p>
     * <b>Fraction modifier</b>: Modifies the pattern that immediately follows to be a fraction.
     * All fractional values must use the 'f' prefix to ensure correct parsing.
     * The fraction also outputs the decimal point.
     * If the count of 'f' is one, then the fractional value has the exact number of digits defined by
     * the count of the value being output.
     * If the count of 'f' is two or more, then the fractional value has the a minimum number of digits
     * defined by the count of the value being output and a maximum output of nine digits.
     * <p>
     * For example, 'ssffnnn' outputs the second followed by 3-9 digits of the nanosecond, while
     * 'mmfss' outputs the minute followed by exactly 2 digits representing the second.
     * <p>
     * <b>Year</b>: The count of letters determines the minimum field width below which padding is used.
     * If the count of letters is two, then a {@link DateTimeFormatterBuilder#appendValueReduced reduced}
     * two digit form is used.
     * For printing, this outputs the rightmost two digits. For parsing, this will parse using the
     * base value of 2000, resulting in a year within the range 2000 to 2099 inclusive.
     * If the count of letters is less than four (but not two), then the sign is only output for negative
     * years as per {@link SignStyle#NORMAL}.
     * Otherwise, the sign is output if the pad width is exceeded, as per {@link SignStyle#EXCEEDS_PAD}
     * <p>
     * <b>ZoneId</b>: 'I' outputs the zone ID, such as 'Europe/Paris'.
     * <p>
     * <b>Offset X</b>: This formats the offset using 'Z' when the offset is zero.
     * One letter outputs just the hour', such as '+01'
     * Two letters outputs the hour and minute, without a colon, such as '+0130'.
     * Three letters outputs the hour and minute, with a colon, such as '+01:30'.
     * Four letters outputs the hour and minute and optional second, without a colon, such as '+013015'.
     * Five letters outputs the hour and minute and optional second, with a colon, such as '+01:30:15'.
     * <p>
     * <b>Offset Z</b>: This formats the offset using '+0000' or '+00:00' when the offset is zero.
     * One or two letters outputs the hour and minute, without a colon, such as '+0130'.
     * Three letters outputs the hour and minute, with a colon, such as '+01:30'.
     * <p>
     * <b>Zone names</b>: Time zone names ('z') cannot be parsed.
     * <p>
     * <b>Optional section</b>: The optional section markers work exactly like calling
     * {@link DateTimeFormatterBuilder#optionalStart()} and {@link DateTimeFormatterBuilder#optionalEnd()}.
     * <p>
     * <b>Pad modifier</b>: Modifies the pattern that immediately follows to be padded with spaces.
     * The pad width is determined by the number of pattern letters.
     * This is the same as calling {@link DateTimeFormatterBuilder#padNext(int)}.
     * <p>
     * For example, 'ppH' outputs the hour-of-day padded on the left with spaces to a width of 2.
     * <p>
     * Any unrecognized letter is an error.
     * Any non-letter character, other than '[', ']' and the single quote will be output directly.
     * Despite this, it is recommended to use single quotes around all characters that you want to
     * output directly to ensure that future changes do not break your application.
     * <p>
     * The pattern string is similar, but not identical, to {@link java.text.SimpleDateFormat SimpleDateFormat}.
     * Pattern letters 'E' and 'u' are merged, which changes the meaning of "E" and "EE" to be numeric.
     * Pattern letter 'W' is not available.
     * Pattern letters 'Z' and 'X' are extended.
     * Pattern letter 'y' and 'Y' parse years of two digits and more than 4 digits differently.
     * Pattern letters 'Q', 'n', 'A', 'N', 'I', 'f' and 'p' are added.
     * Number types will reject large numbers.
     * The pattern string is also similar, but not identical, to that defined by the
     * Unicode Common Locale Data Repository (CLDR).
     *
     * @param pattern  the pattern to use, not null
     * @return the formatter based on the pattern, not null
     * @throws IllegalArgumentException if the pattern is invalid
     * @see DateTimeFormatterBuilder#appendPattern(String)
     */
    public static DateTimeFormatter pattern(String pattern) {
        return new DateTimeFormatterBuilder().appendPattern(pattern).toFormatter();
    }

    /**
     * Creates a formatter using the specified pattern.
     * <p>
     * This method will create a formatter based on a simple pattern of letters and symbols.
     * For example, {@code d MMM yyyy} will format 2011-12-03 as '3 Dec 2011'.
     * <p>
     * See {@link #pattern(String)} for details of the pattern.
     * <p>
     * The returned formatter will use the specified locale, but this can be changed
     * using {@link DateTimeFormatter#withLocale(Locale)}.
     *
     * @param pattern  the pattern to use, not null
     * @param locale  the locale to use, not null
     * @return the formatter based on the pattern, not null
     * @throws IllegalArgumentException if the pattern is invalid
     * @see DateTimeFormatterBuilder#appendPattern(String)
     */
    public static DateTimeFormatter pattern(String pattern, Locale locale) {
        return new DateTimeFormatterBuilder().appendPattern(pattern).toFormatter(locale);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a locale specific date format.
     * <p>
     * This returns a formatter that will print/parse a date.
     * The exact format pattern used varies by locale.
     * <p>
     * The locale is determined from the formatter. The formatter returned directly by
     * this method will use the {@link Locale#getDefault() default locale}.
     * The locale can be controlled using {@link DateTimeFormatter#withLocale(Locale) withLocale(Locale)}
     * on the result of this method.
     * <p>
     * Note that the localized pattern is looked up lazily.
     * This {@code DateTimeFormatter} holds the style required and the locale,
     * looking up the pattern required on demand.
     *
     * @param dateStyle  the formatter style to obtain, not null
     * @return the date formatter, not null
     */
    public static DateTimeFormatter localizedDate(FormatStyle dateStyle) {
        Objects.requireNonNull(dateStyle, "dateStyle");
        return new DateTimeFormatterBuilder().appendLocalized(dateStyle, null).toFormatter();
    }

    /**
     * Returns a locale specific time format.
     * <p>
     * This returns a formatter that will print/parse a time.
     * The exact format pattern used varies by locale.
     * <p>
     * The locale is determined from the formatter. The formatter returned directly by
     * this method will use the {@link Locale#getDefault() default locale}.
     * The locale can be controlled using {@link DateTimeFormatter#withLocale(Locale) withLocale(Locale)}
     * on the result of this method.
     * <p>
     * Note that the localized pattern is looked up lazily.
     * This {@code DateTimeFormatter} holds the style required and the locale,
     * looking up the pattern required on demand.
     *
     * @param timeStyle  the formatter style to obtain, not null
     * @return the time formatter, not null
     */
    public static DateTimeFormatter localizedTime(FormatStyle timeStyle) {
        Objects.requireNonNull(timeStyle, "timeStyle");
        return new DateTimeFormatterBuilder().appendLocalized(null, timeStyle).toFormatter();
    }

    /**
     * Returns a locale specific date-time format, which is typically of short length.
     * <p>
     * This returns a formatter that will print/parse a date-time.
     * The exact format pattern used varies by locale.
     * <p>
     * The locale is determined from the formatter. The formatter returned directly by
     * this method will use the {@link Locale#getDefault() default locale}.
     * The locale can be controlled using {@link DateTimeFormatter#withLocale(Locale) withLocale(Locale)}
     * on the result of this method.
     * <p>
     * Note that the localized pattern is looked up lazily.
     * This {@code DateTimeFormatter} holds the style required and the locale,
     * looking up the pattern required on demand.
     *
     * @param dateTimeStyle  the formatter style to obtain, not null
     * @return the date-time formatter, not null
     */
    public static DateTimeFormatter localizedDateTime(FormatStyle dateTimeStyle) {
        Objects.requireNonNull(dateTimeStyle, "dateTimeStyle");
        return new DateTimeFormatterBuilder().appendLocalized(dateTimeStyle, dateTimeStyle).toFormatter();
    }

    /**
     * Returns a locale specific date and time format.
     * <p>
     * This returns a formatter that will print/parse a date-time.
     * The exact format pattern used varies by locale.
     * <p>
     * The locale is determined from the formatter. The formatter returned directly by
     * this method will use the {@link Locale#getDefault() default locale}.
     * The locale can be controlled using {@link DateTimeFormatter#withLocale(Locale) withLocale(Locale)}
     * on the result of this method.
     * <p>
     * Note that the localized pattern is looked up lazily.
     * This {@code DateTimeFormatter} holds the style required and the locale,
     * looking up the pattern required on demand.
     *
     * @param dateStyle  the date formatter style to obtain, not null
     * @param timeStyle  the time formatter style to obtain, not null
     * @return the date, time or date-time formatter, not null
     */
    public static DateTimeFormatter localizedDateTime(FormatStyle dateStyle, FormatStyle timeStyle) {
        Objects.requireNonNull(dateStyle, "dateStyle");
        Objects.requireNonNull(timeStyle, "timeStyle");
        return new DateTimeFormatterBuilder().appendLocalized(dateStyle, timeStyle).toFormatter();
    }

    //-----------------------------------------------------------------------
    /**
     * Returns the ISO date formatter that prints/parses a local date without
     * an offset, such as '2011-12-03'.
     * <p>
     * This is the ISO-8601 extended format:<br />
     * {@code yyyy-MM-dd}
     * <p>
     * The year will print 4 digits, unless this is insufficient, in which
     * case the full year will be printed together with a positive/negative sign.
     *
     * @return the ISO date formatter, not null
     */
    public static DateTimeFormatter isoLocalDate() {
        return ISO_LOCAL_DATE;
    }

    /** Singleton date formatter. */
    private static final DateTimeFormatter ISO_LOCAL_DATE;
    static {
        ISO_LOCAL_DATE = new DateTimeFormatterBuilder()
            .appendValue(YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
            .appendLiteral('-')
            .appendValue(MONTH_OF_YEAR, 2)
            .appendLiteral('-')
            .appendValue(DAY_OF_MONTH, 2)
            .toFormatter();
    }

    //-----------------------------------------------------------------------
    /**
     * Returns the ISO date formatter that prints/parses an offset date with
     * an offset, such as '2011-12-03+01:00'.
     * <p>
     * This is the ISO-8601 extended format:<br />
     * {@code yyyy-MM-ddZZZ}
     * <p>
     * The year will print 4 digits, unless this is insufficient, in which
     * case the full year will be printed together with a positive/negative sign.
     * <p>
     * The offset will print and parse an offset with seconds even though that
     * is not part of the ISO-8601 standard.
     *
     * @return the ISO date formatter, not null
     */
    public static DateTimeFormatter isoOffsetDate() {
        return ISO_OFFSET_DATE;
    }

    /** Singleton date formatter. */
    private static final DateTimeFormatter ISO_OFFSET_DATE;
    static {
        ISO_OFFSET_DATE = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(ISO_LOCAL_DATE)
            .appendOffsetId()
            .toFormatter();
    }

    //-----------------------------------------------------------------------
    /**
     * Returns the ISO date formatter that prints/parses a date, with the
     * offset and zone if available, such as '2011-12-03', '2011-12-03+01:00'
     * or '2011-12-03+01:00[Europe/Paris]'.
     * <p>
     * This is the ISO-8601 extended format:<br />
     * {@code yyyy-MM-dd[ZZZ['['I']']]}
     * <p>
     * The year will print 4 digits, unless this is insufficient, in which
     * case the full year will be printed together with a positive/negative sign.
     * <p>
     * The offset and time-zone IDs will be printed or parsed if present.
     * The offset will print and parse an offset with seconds even though that
     * is not part of the ISO-8601 standard.
     *
     * @return the ISO date formatter, not null
     */
    public static DateTimeFormatter isoDate() {
        return ISO_DATE;
    }

    /** Singleton date formatter. */
    private static final DateTimeFormatter ISO_DATE;
    static {
        ISO_DATE = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(ISO_LOCAL_DATE)
            .optionalStart()
            .appendOffsetId()
            .optionalStart()
            .appendLiteral('[')
            .appendZoneId()
            .appendLiteral(']')
            .toFormatter();
    }

    //-----------------------------------------------------------------------
    /**
     * Returns the ISO time formatter that prints/parses a local time, without
     * an offset such as '10:15:30'.
     * <p>
     * This is the ISO-8601 extended format:<br />
     * {@code HH:mm[:ss[.S]]}
     * <p>
     * The seconds will be printed if present in the input, thus a {@code LocalTime}
     * will always print the seconds.
     * The nanoseconds will be printed if non-zero.
     * If non-zero, the minimum number of fractional second digits will printed.
     *
     * @return the ISO time formatter, not null
     */
    public static DateTimeFormatter isoLocalTime() {
        return ISO_LOCAL_TIME;
    }

    /** Singleton date formatter. */
    private static final DateTimeFormatter ISO_LOCAL_TIME;
    static {
        ISO_LOCAL_TIME = new DateTimeFormatterBuilder()
            .appendValue(HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(MINUTE_OF_HOUR, 2)
            .optionalStart()
            .appendLiteral(':')
            .appendValue(SECOND_OF_MINUTE, 2)
            .optionalStart()
            .appendFraction(NANO_OF_SECOND, 0, 9)
            .toFormatter();
    }

    //-----------------------------------------------------------------------
    /**
     * Returns the ISO time formatter that prints/parses a local time, with
     * an offset such as '10:15:30+01:00'.
     * <p>
     * This is the ISO-8601 extended format:<br />
     * {@code HH:mm[:ss[.S]]ZZZ}
     * <p>
     * The seconds will be printed if present in the input, thus an {@code OffsetTime}
     * will always print the seconds.
     * The nanoseconds will be printed if non-zero.
     * If non-zero, the minimum number of fractional second digits will printed.
     * <p>
     * The offset will print and parse an offset with seconds even though that
     * is not part of the ISO-8601 standard.
     *
     * @return the ISO time formatter, not null
     */
    public static DateTimeFormatter isoOffsetTime() {
        return ISO_OFFSET_TIME;
    }

    /** Singleton date formatter. */
    private static final DateTimeFormatter ISO_OFFSET_TIME;
    static {
        ISO_OFFSET_TIME = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(ISO_LOCAL_TIME)
            .appendOffsetId()
            .toFormatter();
    }

    //-----------------------------------------------------------------------
    /**
     * Returns the ISO time formatter that prints/parses a time, with the
     * offset and zone if available, such as '10:15:30', '10:15:30+01:00'
     * or '10:15:30+01:00[Europe/Paris]'.
     * <p>
     * This is the ISO-8601 extended format:<br />
     * {@code HH:mm[:ss[.S]][ZZZ['['I']']]}
     * <p>
     * The seconds will be printed if present in the input, thus a {@code LocalTime}
     * will always print the seconds.
     * The nanoseconds will be printed if non-zero.
     * If non-zero, the minimum number of fractional second digits will printed.
     * <p>
     * The offset and time-zone IDs will be printed or parsed if present.
     * The offset will print and parse an offset with seconds even though that
     * is not part of the ISO-8601 standard.
     *
     * @return the ISO date formatter, not null
     */
    public static DateTimeFormatter isoTime() {
        return ISO_TIME;
    }

    /** Singleton date formatter. */
    private static final DateTimeFormatter ISO_TIME;
    static {
        ISO_TIME = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(ISO_LOCAL_TIME)
            .optionalStart()
            .appendOffsetId()
            .optionalStart()
            .appendLiteral('[')
            .appendZoneId()
            .appendLiteral(']')
            .toFormatter();
    }

    //-----------------------------------------------------------------------
    /**
     * Returns the ISO date formatter that prints/parses a local date without
     * an offset, such as '2011-12-03T10:15:30'.
     * <p>
     * This is the ISO-8601 extended format:<br />
     * {@code yyyy-MM-dd'T'HH:mm[:ss[.S]]}
     * <p>
     * The year will print 4 digits, unless this is insufficient, in which
     * case the full year will be printed together with a positive/negative sign.
     * <p>
     * The seconds will be printed if present in the input, thus a {@code LocalDateTime}
     * will always print the seconds.
     * The nanoseconds will be printed if non-zero.
     * If non-zero, the minimum number of fractional second digits will printed.
     *
     * @return the ISO date formatter, not null
     */
    public static DateTimeFormatter isoLocalDateTime() {
        return ISO_LOCAL_DATE_TIME;
    }

    /** Singleton date formatter. */
    private static final DateTimeFormatter ISO_LOCAL_DATE_TIME;
    static {
        ISO_LOCAL_DATE_TIME = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(ISO_LOCAL_DATE)
            .appendLiteral('T')
            .append(ISO_LOCAL_TIME)
            .toFormatter();
    }

    //-----------------------------------------------------------------------
    /**
     * Returns the ISO date formatter that prints/parses an offset date with
     * an offset, such as '2011-12-03T10:15:30+01:00'.
     * <p>
     * This is the ISO-8601 extended format:<br />
     * {@code yyyy-MM-dd'T'HH:mm[:ss[.S]]ZZZ}
     * <p>
     * The year will print 4 digits, unless this is insufficient, in which
     * case the full year will be printed together with a positive/negative sign.
     * <p>
     * The seconds will be printed if present in the input, thus an {@code OffsetDateTime}
     * will always print the seconds.
     * The nanoseconds will be printed if non-zero.
     * If non-zero, the minimum number of fractional second digits will printed.
     * <p>
     * The offset will print and parse an offset with seconds even though that
     * is not part of the ISO-8601 standard.
     *
     * @return the ISO date formatter, not null
     */
    public static DateTimeFormatter isoOffsetDateTime() {
        return ISO_OFFSET_DATE_TIME;
    }

    /** Singleton date formatter. */
    private static final DateTimeFormatter ISO_OFFSET_DATE_TIME;
    static {
        ISO_OFFSET_DATE_TIME = new DateTimeFormatterBuilder()
            .append(ISO_LOCAL_DATE_TIME)
            .appendOffsetId()
            .toFormatter();
    }

    //-----------------------------------------------------------------------
    /**
     * Returns the ISO date formatter that prints/parses an offset date with
     * a zone, such as '2011-12-03T10:15:30+01:00[Europe/Paris]'.
     * <p>
     * This is the ISO-8601 extended format:<br />
     * {@code yyyy-MM-dd'T'HH:mm[:ss[.S]]ZZZ'['I']'}
     * <p>
     * The year will print 4 digits, unless this is insufficient, in which
     * case the full year will be printed together with a positive/negative sign.
     * <p>
     * The seconds will be printed if present in the input, thus a {@code ZonedDateTime}
     * will always print the seconds.
     * The nanoseconds will be printed if non-zero.
     * If non-zero, the minimum number of fractional second digits will printed.
     * <p>
     * The offset will print and parse an offset with seconds even though that
     * is not part of the ISO-8601 standard.
     *
     * @return the ISO date formatter, not null
     */
    public static DateTimeFormatter isoZonedDateTime() {
        return ISO_ZONED_DATE_TIME;
    }

    /** Singleton date formatter. */
    private static final DateTimeFormatter ISO_ZONED_DATE_TIME;
    static {
        ISO_ZONED_DATE_TIME = new DateTimeFormatterBuilder()
            .append(ISO_LOCAL_DATE_TIME)
            .appendOffsetId()
            .appendLiteral('[')
            .appendZoneId()
            .appendLiteral(']')
            .toFormatter();
    }

    //-----------------------------------------------------------------------
    /**
     * Returns the ISO date formatter that prints/parses a date, with the
     * offset and zone if available, such as '2011-12-03T10:15:30',
     * '2011-12-03T10:15:30+01:00' or '2011-12-03T10:15:30+01:00[Europe/Paris]'.
     * <p>
     * This is the ISO-8601 extended format:<br />
     * {@code yyyy-MM-dd'T'HH:mm[:ss[.S]][ZZZ['['I']']]}
     * <p>
     * The year will print 4 digits, unless this is insufficient, in which
     * case the full year will be printed together with a positive/negative sign.
     * <p>
     * The seconds will be printed if present in the input, thus a {@code ZonedDateTime}
     * will always print the seconds.
     * The nanoseconds will be printed if non-zero.
     * If non-zero, the minimum number of fractional second digits will printed.
     * <p>
     * The offset and time-zone IDs will be printed or parsed if present.
     * The offset will print and parse an offset with seconds even though that
     * is not part of the ISO-8601 standard.
     *
     * @return the ISO date formatter, not null
     */
    public static DateTimeFormatter isoDateTime() {
        return ISO_DATE_TIME;
    }

    /** Singleton date formatter. */
    private static final DateTimeFormatter ISO_DATE_TIME;
    static {
        ISO_DATE_TIME = new DateTimeFormatterBuilder()
            .append(ISO_LOCAL_DATE_TIME)
            .optionalStart()
            .appendOffsetId()
            .optionalStart()
            .appendLiteral('[')
            .appendZoneId()
            .appendLiteral(']')
            .toFormatter();
    }

    //-----------------------------------------------------------------------
    /**
     * Returns the ISO date formatter that prints/parses a date without an offset.
     * <p>
     * This is the ISO-8601 extended format:<br />
     * {@code yyyy-DDD[ZZZ['['I']']]}
     * <p>
     * The year will print 4 digits, unless this is insufficient, in which
     * case the full year will be printed together with a positive/negative sign.
     * <p>
     * The offset and time-zone IDs will be printed or parsed if present.
     * The offset will print and parse an offset with seconds even though that
     * is not part of the ISO-8601 standard.
     *
     * @return the ISO ordinal date formatter, not null
     */
    public static DateTimeFormatter isoOrdinalDate() {
        return ISO_ORDINAL_DATE;
    }

    /** Singleton date formatter. */
    private static final DateTimeFormatter ISO_ORDINAL_DATE;
    static {
        ISO_ORDINAL_DATE = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendValue(YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
            .appendLiteral('-')
            .appendValue(DAY_OF_YEAR, 3)
            .optionalStart()
            .appendOffsetId()
            .optionalStart()
            .appendLiteral('[')
            .appendZoneId()
            .appendLiteral(']')
            .toFormatter();
    }

    //-----------------------------------------------------------------------
    /**
     * Returns the ISO date formatter that prints/parses a date without an offset.
     * <p>
     * This is the ISO-8601 extended format:<br />
     * {@code yyyy-Www-D[ZZZ['['I']']]}
     * <p>
     * The year will print 4 digits, unless this is insufficient, in which
     * case the full year will be printed together with a positive/negative sign.
     * <p>
     * The offset and time-zone IDs will be printed or parsed if present.
     * The offset will print and parse an offset with seconds even though that
     * is not part of the ISO-8601 standard.
     *
     * @return the ISO week date formatter, not null
     */
    public static DateTimeFormatter isoWeekDate() {
        return ISO_WEEK_DATE;
    }

    /** Singleton date formatter. */
    private static final DateTimeFormatter ISO_WEEK_DATE;
    static {
        ISO_WEEK_DATE = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendValue(ISOWeeks.WEEK_BASED_YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
            .appendLiteral("-W")
            .appendValue(ISOWeeks.WEEK_OF_WEEK_BASED_YEAR, 2)
            .appendLiteral('-')
            .appendValue(DAY_OF_WEEK, 1)
            .optionalStart()
            .appendOffsetId()
            .optionalStart()
            .appendLiteral('[')
            .appendZoneId()
            .appendLiteral(']')
            .toFormatter();
    }

    //-----------------------------------------------------------------------
    /**
     * Returns the ISO instant formatter that prints/parses an instant in UTC.
     * <p>
     * This is the ISO-8601 extended format:<br />
     * {@code yyyy-MM-dd'T'HH:mm:ss.SZZZ}
     * <p>
     * The year will print 4 digits, unless this is insufficient, in which
     * case the full year will be printed together with a positive/negative sign.
     * <p>
     * The offset will print and parse an offset with seconds even though that
     * is not part of the ISO-8601 standard.
     *
     * @return the ISO instant formatter, not null
     */
    public static DateTimeFormatter isoInstant() {
        return ISO_INSTANT;
    }

    /** Singleton formatter. */
    private static final DateTimeFormatter ISO_INSTANT;
    static {
        ISO_INSTANT = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendInstant()
            .toFormatter();
    }

    //-----------------------------------------------------------------------
    /**
     * Returns the ISO date formatter that prints/parses a date without an offset.
     * <p>
     * This is the ISO-8601 extended format:<br />
     * {@code yyyyMMdd}
     * <p>
     * The year is limited to printing and parsing 4 digits, as the lack of
     * separators makes it impossible to parse more than 4 digits.
     *
     * @return the ISO date formatter, not null
     */
    public static DateTimeFormatter basicIsoDate() {
        return BASIC_ISO_DATE;
    }

    /** Singleton date formatter. */
    private static final DateTimeFormatter BASIC_ISO_DATE;
    static {
        BASIC_ISO_DATE = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendValue(YEAR, 4)
            .appendValue(MONTH_OF_YEAR, 2)
            .appendValue(DAY_OF_MONTH, 2)
            .optionalStart()
            .appendOffset("Z", "+HHMM")
            .optionalStart()
            .appendLiteral('[')
            .appendZoneId()
            .appendLiteral(']')
            .toFormatter();
    }

    //-----------------------------------------------------------------------
    /**
     * Returns the RFC-1123 date-time formatter.
     * <p>
     * This is the RFC-1123 format: EEE, dd MMM yyyy HH:mm:ss Z.
     * This is the updated replacement for RFC-822 which had a two digit year.
     * <p>
     * The year will print 4 digits, and only the range 0000 to 9999 is supported.
     *
     * @return the ISO date formatter, not null
     */
    public static DateTimeFormatter rfc1123() {
        return RFC_1123_DATE_TIME;
    }

    /** Singleton date formatter. */
    private static final DateTimeFormatter RFC_1123_DATE_TIME;
    static {
        RFC_1123_DATE_TIME = new DateTimeFormatterBuilder()
            .appendText(DAY_OF_WEEK, TextStyle.SHORT)
            .appendLiteral(", ")
            .appendValue(DAY_OF_MONTH, 2)
            .appendLiteral(' ')
            .appendText(MONTH_OF_YEAR, TextStyle.SHORT)
            .appendLiteral(' ')
            .appendValue(YEAR, 4, 4, SignStyle.NOT_NEGATIVE)
            .appendLiteral(' ')
            .appendValue(HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(MINUTE_OF_HOUR, 2)
            .appendLiteral(':')
            .appendValue(SECOND_OF_MINUTE, 2)
            .appendLiteral(' ')
            .appendOffset("Z", "+HHMM")
            .toFormatter()
            .withLocale(Locale.ENGLISH);
    }

    //-------------------------------------------------------------------------
    /**
     * Gets the provider of text.
     *
     * @return the provider, not null
     */
    public static DateTimeTextProvider getTextProvider() {
        // TODO: obtain provider properly
        return new SimpleDateTimeTextProvider();
    }

    /**
     * Gets the provider of format styles.
     *
     * @return the provider, not null
     */
    public static DateTimeFormatStyleProvider getFormatStyleProvider() {
        // TODO: obtain provider properly
        return new SimpleDateTimeFormatStyleProvider();
    }

}
