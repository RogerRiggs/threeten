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
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.text.ParsePosition;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.time.DateTimeException;
import javax.time.LocalDate;
import javax.time.LocalDateTime;
import javax.time.OffsetDateTime;
import javax.time.Year;
import javax.time.YearMonth;
import javax.time.ZoneId;
import javax.time.ZoneOffset;
import javax.time.ZonedDateTime;
import javax.time.calendrical.DateTimeAccessor;
import javax.time.calendrical.DateTimeBuilder;
import javax.time.calendrical.DateTimeField;
import javax.time.calendrical.ISOWeeks;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test DateTimeFormatters.
 */
@Test
public class TCKDateTimeFormatters {

    @BeforeMethod
    public void setUp() {
    }

    //-----------------------------------------------------------------------
    @Test(expectedExceptions=NullPointerException.class, groups={"tck"})
    public void test_print_nullCalendrical() {
        DateTimeFormatters.isoDate().print((DateTimeAccessor) null);
    }

    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_pattern_String() {
        DateTimeFormatter test = DateTimeFormatters.pattern("d MMM yyyy");
        assertEquals(test.toString(), "Value(DayOfMonth)' 'Text(MonthOfYear,SHORT)' 'Value(Year,4,19,EXCEEDS_PAD)");
        assertEquals(test.getLocale(), Locale.getDefault());
    }

    @Test(expectedExceptions=IllegalArgumentException.class, groups={"tck"})
    public void test_pattern_String_invalid() {
        DateTimeFormatters.pattern("p");
    }

    @Test(expectedExceptions=NullPointerException.class, groups={"tck"})
    public void test_pattern_String_null() {
        DateTimeFormatters.pattern(null);
    }

    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_pattern_StringLocale() {
        DateTimeFormatter test = DateTimeFormatters.pattern("d MMM yyyy", Locale.UK);
        assertEquals(test.toString(), "Value(DayOfMonth)' 'Text(MonthOfYear,SHORT)' 'Value(Year,4,19,EXCEEDS_PAD)");
        assertEquals(test.getLocale(), Locale.UK);
    }

    @Test(expectedExceptions=IllegalArgumentException.class, groups={"tck"})
    public void test_pattern_StringLocale_invalid() {
        DateTimeFormatters.pattern("p", Locale.UK);
    }

    @Test(expectedExceptions=NullPointerException.class, groups={"tck"})
    public void test_pattern_StringLocale_nullPattern() {
        DateTimeFormatters.pattern(null, Locale.UK);
    }

    @Test(expectedExceptions=NullPointerException.class, groups={"tck"})
    public void test_pattern_StringLocale_nullLocale() {
        DateTimeFormatters.pattern("yyyy", null);
    }

    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    @DataProvider(name="sample_isoLocalDate")
    Object[][] provider_sample_isoLocalDate() {
        return new Object[][]{
                {2008, null, null, null, null, null, DateTimeException.class},
                {null, 6, null, null, null, null, DateTimeException.class},
                {null, null, 30, null, null, null, DateTimeException.class},
                {null, null, null, "+01:00", null, null, DateTimeException.class},
                {null, null, null, null, "Europe/Paris", null, DateTimeException.class},
                {2008, 6, null, null, null, null, DateTimeException.class},
                {null, 6, 30, null, null, null, DateTimeException.class},

                {2008, 6, 30, null, null,                   "2008-06-30", null},
                {2008, 6, 30, "+01:00", null,               "2008-06-30", null},
                {2008, 6, 30, "+01:00", "Europe/Paris",     "2008-06-30", null},
                {2008, 6, 30, null, "Europe/Paris",         "2008-06-30", null},

                {123456, 6, 30, null, null,                 "+123456-06-30", null},
        };
    }

    @Test(dataProvider="sample_isoLocalDate", groups={"tck"})
    public void test_print_isoLocalDate(
            Integer year, Integer month, Integer day, String offsetId, String zoneId,
            String expected, Class<?> expectedEx) {
        DateTimeBuilder test = createDate(year, month, day);
        buildCalendrical(test, offsetId, zoneId);
        if (expectedEx == null) {
            assertEquals(DateTimeFormatters.isoLocalDate().print(test), expected);
        } else {
            try {
                DateTimeFormatters.isoLocalDate().print(test);
                fail();
            } catch (Exception ex) {
                assertTrue(expectedEx.isInstance(ex));
            }
        }
    }

    @Test(dataProvider="sample_isoLocalDate", groups={"tck"})
    public void test_parse_isoLocalDate(
            Integer year, Integer month, Integer day, String offsetId, String zoneId,
            String input, Class<?> invalid) {
        if (input != null) {
            DateTimeBuilder expected = createDate(year, month, day);
            // offset/zone not expected to be parsed
            assertParseMatch(DateTimeFormatters.isoLocalDate().parseToBuilder(input, new ParsePosition(0)), expected);
        }
    }

    @Test(groups={"tck"})
    public void test_parse_isoLocalDate_999999999() {
        DateTimeBuilder expected = createDate(999999999, 8, 6);
        assertParseMatch(DateTimeFormatters.isoLocalDate().parseToBuilder("+999999999-08-06", new ParsePosition(0)), expected);
        assertEquals(LocalDate.parse("+999999999-08-06"), LocalDate.of(999999999, 8, 6));
    }

    @Test(groups={"tck"})
    public void test_parse_isoLocalDate_1000000000() {
        DateTimeBuilder expected = createDate(1000000000, 8, 6);
        assertParseMatch(DateTimeFormatters.isoLocalDate().parseToBuilder("+1000000000-08-06", new ParsePosition(0)), expected);
    }

    @Test(expectedExceptions = DateTimeException.class, groups={"tck"})
    public void test_parse_isoLocalDate_1000000000_failedCreate() {
        LocalDate.parse("+1000000000-08-06");
    }

    @Test(groups={"tck"})
    public void test_parse_isoLocalDate_M999999999() {
        DateTimeBuilder expected = createDate(-999999999, 8, 6);
        assertParseMatch(DateTimeFormatters.isoLocalDate().parseToBuilder("-999999999-08-06", new ParsePosition(0)), expected);
        assertEquals(LocalDate.parse("-999999999-08-06"), LocalDate.of(-999999999, 8, 6));
    }

    @Test(groups={"tck"})
    public void test_parse_isoLocalDate_M1000000000() {
        DateTimeBuilder expected = createDate(-1000000000, 8, 6);
        assertParseMatch(DateTimeFormatters.isoLocalDate().parseToBuilder("-1000000000-08-06", new ParsePosition(0)), expected);
    }

    @Test(expectedExceptions = DateTimeException.class, groups={"tck"})
    public void test_parse_isoLocalDate_M1000000000_failedCreate() {
        LocalDate.parse("-1000000000-08-06");
    }

    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    @DataProvider(name="sample_isoOffsetDate")
    Object[][] provider_sample_isoOffsetDate() {
        return new Object[][]{
                {2008, null, null, null, null, null, DateTimeException.class},
                {null, 6, null, null, null, null, DateTimeException.class},
                {null, null, 30, null, null, null, DateTimeException.class},
                {null, null, null, "+01:00", null, null, DateTimeException.class},
                {null, null, null, null, "Europe/Paris", null, DateTimeException.class},
                {2008, 6, null, null, null, null, DateTimeException.class},
                {null, 6, 30, null, null, null, DateTimeException.class},

                {2008, 6, 30, null, null,                   null, DateTimeException.class},
                {2008, 6, 30, "+01:00", null,               "2008-06-30+01:00", null},
                {2008, 6, 30, "+01:00", "Europe/Paris",     "2008-06-30+01:00", null},
                {2008, 6, 30, null, "Europe/Paris",         null, DateTimeException.class},

                {123456, 6, 30, "+01:00", null,             "+123456-06-30+01:00", null},
        };
    }

    @Test(dataProvider="sample_isoOffsetDate", groups={"tck"})
    public void test_print_isoOffsetDate(
            Integer year, Integer month, Integer day, String offsetId, String zoneId,
            String expected, Class<?> expectedEx) {
        DateTimeBuilder test = createDate(year, month, day);
        buildCalendrical(test, offsetId, zoneId);
        if (expectedEx == null) {
            assertEquals(DateTimeFormatters.isoOffsetDate().print(test), expected);
        } else {
            try {
                DateTimeFormatters.isoOffsetDate().print(test);
                fail();
            } catch (Exception ex) {
                assertTrue(expectedEx.isInstance(ex));
            }
        }
    }

    @Test(dataProvider="sample_isoOffsetDate", groups={"tck"})
    public void test_parse_isoOffsetDate(
            Integer year, Integer month, Integer day, String offsetId, String zoneId,
            String input, Class<?> invalid) {
        if (input != null) {
            DateTimeBuilder expected = createDate(year, month, day);
            buildCalendrical(expected, offsetId, null);  // zone not expected to be parsed
            assertParseMatch(DateTimeFormatters.isoOffsetDate().parseToBuilder(input, new ParsePosition(0)), expected);
        }
    }

    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    @DataProvider(name="sample_isoDate")
    Object[][] provider_sample_isoDate() {
        return new Object[][]{
                {2008, null, null, null, null, null, DateTimeException.class},
                {null, 6, null, null, null, null, DateTimeException.class},
                {null, null, 30, null, null, null, DateTimeException.class},
                {null, null, null, "+01:00", null, null, DateTimeException.class},
                {null, null, null, null, "Europe/Paris", null, DateTimeException.class},
                {2008, 6, null, null, null, null, DateTimeException.class},
                {null, 6, 30, null, null, null, DateTimeException.class},

                {2008, 6, 30, null, null,                   "2008-06-30", null},
                {2008, 6, 30, "+01:00", null,               "2008-06-30+01:00", null},
                {2008, 6, 30, "+01:00", "Europe/Paris",     "2008-06-30+01:00[Europe/Paris]", null},
                {2008, 6, 30, null, "Europe/Paris",         "2008-06-30", null},

                {123456, 6, 30, "+01:00", "Europe/Paris",   "+123456-06-30+01:00[Europe/Paris]", null},
        };
    }

    @Test(dataProvider="sample_isoDate", groups={"tck"})
    public void test_print_isoDate(
            Integer year, Integer month, Integer day, String offsetId, String zoneId,
            String expected, Class<?> expectedEx) {
        DateTimeBuilder test = createDate(year, month, day);
        buildCalendrical(test, offsetId, zoneId);
        if (expectedEx == null) {
            assertEquals(DateTimeFormatters.isoDate().print(test), expected);
        } else {
            try {
                DateTimeFormatters.isoDate().print(test);
                fail();
            } catch (Exception ex) {
                assertTrue(expectedEx.isInstance(ex));
            }
        }
    }

    @Test(dataProvider="sample_isoDate", groups={"tck"})
    public void test_parse_isoDate(
            Integer year, Integer month, Integer day, String offsetId, String zoneId,
            String input, Class<?> invalid) {
        if (input != null) {
            DateTimeBuilder expected = createDate(year, month, day);
            if (offsetId != null) {
                expected.addCalendrical(ZoneOffset.of(offsetId));
                if (zoneId != null) {
                    expected.addCalendrical(ZoneId.of(zoneId));
                }
            }
            assertParseMatch(DateTimeFormatters.isoDate().parseToBuilder(input, new ParsePosition(0)), expected);
        }
    }

    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    @DataProvider(name="sample_isoLocalTime")
    Object[][] provider_sample_isoLocalTime() {
        return new Object[][]{
                {11, null, null, null, null, null, null, DateTimeException.class},
                {null, 5, null, null, null, null, null, DateTimeException.class},
                {null, null, 30, null, null, null, null, DateTimeException.class},
                {null, null, null, 1, null, null, null, DateTimeException.class},
                {null, null, null, null, "+01:00", null, null, DateTimeException.class},
                {null, null, null, null, null, "Europe/Paris", null, DateTimeException.class},

                {11, 5, null, null, null, null,     "11:05", null},
                {11, 5, 30, null, null, null,       "11:05:30", null},
                {11, 5, 30, 500000000, null, null,  "11:05:30.5", null},
                {11, 5, 30, 1, null, null,          "11:05:30.000000001", null},

                {11, 5, null, null, "+01:00", null,     "11:05", null},
                {11, 5, 30, null, "+01:00", null,       "11:05:30", null},
                {11, 5, 30, 500000000, "+01:00", null,  "11:05:30.5", null},
                {11, 5, 30, 1, "+01:00", null,          "11:05:30.000000001", null},

                {11, 5, null, null, "+01:00", "Europe/Paris",       "11:05", null},
                {11, 5, 30, null, "+01:00", "Europe/Paris",         "11:05:30", null},
                {11, 5, 30, 500000000, "+01:00", "Europe/Paris",    "11:05:30.5", null},
                {11, 5, 30, 1, "+01:00", "Europe/Paris",            "11:05:30.000000001", null},

                {11, 5, null, null, null, "Europe/Paris",       "11:05", null},
                {11, 5, 30, null, null, "Europe/Paris",         "11:05:30", null},
                {11, 5, 30, 500000000, null, "Europe/Paris",    "11:05:30.5", null},
                {11, 5, 30, 1, null, "Europe/Paris",            "11:05:30.000000001", null},
        };
    }

    @Test(dataProvider="sample_isoLocalTime", groups={"tck"})
    public void test_print_isoLocalTime(
            Integer hour, Integer min, Integer sec, Integer nano, String offsetId, String zoneId,
            String expected, Class<?> expectedEx) {
        DateTimeBuilder test = createTime(hour, min, sec, nano);
        buildCalendrical(test, offsetId, zoneId);
        if (expectedEx == null) {
            assertEquals(DateTimeFormatters.isoLocalTime().print(test), expected);
        } else {
            try {
                DateTimeFormatters.isoLocalTime().print(test);
                fail();
            } catch (Exception ex) {
                assertTrue(expectedEx.isInstance(ex));
            }
        }
    }

    @Test(dataProvider="sample_isoLocalTime", groups={"tck"})
    public void test_parse_isoLocalTime(
            Integer hour, Integer min, Integer sec, Integer nano, String offsetId, String zoneId,
            String input, Class<?> invalid) {
        if (input != null) {
            DateTimeBuilder expected = createTime(hour, min, sec, nano);
            // offset/zone not expected to be parsed
            assertParseMatch(DateTimeFormatters.isoLocalTime().parseToBuilder(input, new ParsePosition(0)), expected);
        }
    }

    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    @DataProvider(name="sample_isoOffsetTime")
    Object[][] provider_sample_isoOffsetTime() {
        return new Object[][]{
                {11, null, null, null, null, null, null, DateTimeException.class},
                {null, 5, null, null, null, null, null, DateTimeException.class},
                {null, null, 30, null, null, null, null, DateTimeException.class},
                {null, null, null, 1, null, null, null, DateTimeException.class},
                {null, null, null, null, "+01:00", null, null, DateTimeException.class},
                {null, null, null, null, null, "Europe/Paris", null, DateTimeException.class},

                {11, 5, null, null, null, null,     null, DateTimeException.class},
                {11, 5, 30, null, null, null,       null, DateTimeException.class},
                {11, 5, 30, 500000000, null, null,  null, DateTimeException.class},
                {11, 5, 30, 1, null, null,          null, DateTimeException.class},

                {11, 5, null, null, "+01:00", null,     "11:05+01:00", null},
                {11, 5, 30, null, "+01:00", null,       "11:05:30+01:00", null},
                {11, 5, 30, 500000000, "+01:00", null,  "11:05:30.5+01:00", null},
                {11, 5, 30, 1, "+01:00", null,          "11:05:30.000000001+01:00", null},

                {11, 5, null, null, "+01:00", "Europe/Paris",       "11:05+01:00", null},
                {11, 5, 30, null, "+01:00", "Europe/Paris",         "11:05:30+01:00", null},
                {11, 5, 30, 500000000, "+01:00", "Europe/Paris",    "11:05:30.5+01:00", null},
                {11, 5, 30, 1, "+01:00", "Europe/Paris",            "11:05:30.000000001+01:00", null},

                {11, 5, null, null, null, "Europe/Paris",       null, DateTimeException.class},
                {11, 5, 30, null, null, "Europe/Paris",         null, DateTimeException.class},
                {11, 5, 30, 500000000, null, "Europe/Paris",    null, DateTimeException.class},
                {11, 5, 30, 1, null, "Europe/Paris",            null, DateTimeException.class},
        };
    }

    @Test(dataProvider="sample_isoOffsetTime", groups={"tck"})
    public void test_print_isoOffsetTime(
            Integer hour, Integer min, Integer sec, Integer nano, String offsetId, String zoneId,
            String expected, Class<?> expectedEx) {
        DateTimeBuilder test = createTime(hour, min, sec, nano);
        buildCalendrical(test, offsetId, zoneId);
        if (expectedEx == null) {
            assertEquals(DateTimeFormatters.isoOffsetTime().print(test), expected);
        } else {
            try {
                DateTimeFormatters.isoOffsetTime().print(test);
                fail();
            } catch (Exception ex) {
                assertTrue(expectedEx.isInstance(ex));
            }
        }
    }

    @Test(dataProvider="sample_isoOffsetTime", groups={"tck"})
    public void test_parse_isoOffsetTime(
            Integer hour, Integer min, Integer sec, Integer nano, String offsetId, String zoneId,
            String input, Class<?> invalid) {
        if (input != null) {
            DateTimeBuilder expected = createTime(hour, min, sec, nano);
            buildCalendrical(expected, offsetId, null);  // zoneId is not expected from parse
            assertParseMatch(DateTimeFormatters.isoOffsetTime().parseToBuilder(input, new ParsePosition(0)), expected);
        }
    }

    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    @DataProvider(name="sample_isoTime")
    Object[][] provider_sample_isoTime() {
        return new Object[][]{
                {11, null, null, null, null, null, null, DateTimeException.class},
                {null, 5, null, null, null, null, null, DateTimeException.class},
                {null, null, 30, null, null, null, null, DateTimeException.class},
                {null, null, null, 1, null, null, null, DateTimeException.class},
                {null, null, null, null, "+01:00", null, null, DateTimeException.class},
                {null, null, null, null, null, "Europe/Paris", null, DateTimeException.class},

                {11, 5, null, null, null, null,     "11:05", null},
                {11, 5, 30, null, null, null,       "11:05:30", null},
                {11, 5, 30, 500000000, null, null,  "11:05:30.5", null},
                {11, 5, 30, 1, null, null,          "11:05:30.000000001", null},

                {11, 5, null, null, "+01:00", null,     "11:05+01:00", null},
                {11, 5, 30, null, "+01:00", null,       "11:05:30+01:00", null},
                {11, 5, 30, 500000000, "+01:00", null,  "11:05:30.5+01:00", null},
                {11, 5, 30, 1, "+01:00", null,          "11:05:30.000000001+01:00", null},

                {11, 5, null, null, "+01:00", "Europe/Paris",       "11:05+01:00[Europe/Paris]", null},
                {11, 5, 30, null, "+01:00", "Europe/Paris",         "11:05:30+01:00[Europe/Paris]", null},
                {11, 5, 30, 500000000, "+01:00", "Europe/Paris",    "11:05:30.5+01:00[Europe/Paris]", null},
                {11, 5, 30, 1, "+01:00", "Europe/Paris",            "11:05:30.000000001+01:00[Europe/Paris]", null},

                {11, 5, null, null, null, "Europe/Paris",       "11:05", null},
                {11, 5, 30, null, null, "Europe/Paris",         "11:05:30", null},
                {11, 5, 30, 500000000, null, "Europe/Paris",    "11:05:30.5", null},
                {11, 5, 30, 1, null, "Europe/Paris",            "11:05:30.000000001", null},
        };
    }

    @Test(dataProvider="sample_isoTime", groups={"tck"})
    public void test_print_isoTime(
            Integer hour, Integer min, Integer sec, Integer nano, String offsetId, String zoneId,
            String expected, Class<?> expectedEx) {
        DateTimeBuilder test = createTime(hour, min, sec, nano);
        buildCalendrical(test, offsetId, zoneId);
        if (expectedEx == null) {
            assertEquals(DateTimeFormatters.isoTime().print(test), expected);
        } else {
            try {
                DateTimeFormatters.isoTime().print(test);
                fail();
            } catch (Exception ex) {
                assertTrue(expectedEx.isInstance(ex));
            }
        }
    }

    @Test(dataProvider="sample_isoTime", groups={"tck"})
    public void test_parse_isoTime(
            Integer hour, Integer min, Integer sec, Integer nano, String offsetId, String zoneId,
            String input, Class<?> invalid) {
        if (input != null) {
            DateTimeBuilder expected = createTime(hour, min, sec, nano);
            if (offsetId != null) {
                expected.addCalendrical(ZoneOffset.of(offsetId));
                if (zoneId != null) {
                    expected.addCalendrical(ZoneId.of(zoneId));
                }
            }
            assertParseMatch(DateTimeFormatters.isoTime().parseToBuilder(input, new ParsePosition(0)), expected);
        }
    }

    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    @DataProvider(name="sample_isoLocalDateTime")
    Object[][] provider_sample_isoLocalDateTime() {
        return new Object[][]{
                {2008, null, null, null, null, null, null, null, null, null, DateTimeException.class},
                {null, 6, null, null, null, null, null, null, null, null, DateTimeException.class},
                {null, null, 30, null, null, null, null, null, null, null, DateTimeException.class},
                {null, null, null, 11, null, null, null, null, null, null, DateTimeException.class},
                {null, null, null, null, 5, null, null, null, null, null, DateTimeException.class},
                {null, null, null, null, null, null, null, "+01:00", null, null, DateTimeException.class},
                {null, null, null, null, null, null, null, null, "Europe/Paris", null, DateTimeException.class},
                {2008, 6, 30, 11, null, null, null, null, null, null, DateTimeException.class},
                {2008, 6, 30, null, 5, null, null, null, null, null, DateTimeException.class},
                {2008, 6, null, 11, 5, null, null, null, null, null, DateTimeException.class},
                {2008, null, 30, 11, 5, null, null, null, null, null, DateTimeException.class},
                {null, 6, 30, 11, 5, null, null, null, null, null, DateTimeException.class},

                {2008, 6, 30, 11, 5, null, null, null, null,                    "2008-06-30T11:05", null},
                {2008, 6, 30, 11, 5, 30, null, null, null,                      "2008-06-30T11:05:30", null},
                {2008, 6, 30, 11, 5, 30, 500000000, null, null,                 "2008-06-30T11:05:30.5", null},
                {2008, 6, 30, 11, 5, 30, 1, null, null,                         "2008-06-30T11:05:30.000000001", null},

                {2008, 6, 30, 11, 5, null, null, "+01:00", null,                "2008-06-30T11:05", null},
                {2008, 6, 30, 11, 5, 30, null, "+01:00", null,                  "2008-06-30T11:05:30", null},
                {2008, 6, 30, 11, 5, 30, 500000000, "+01:00", null,             "2008-06-30T11:05:30.5", null},
                {2008, 6, 30, 11, 5, 30, 1, "+01:00", null,                     "2008-06-30T11:05:30.000000001", null},

                {2008, 6, 30, 11, 5, null, null, "+01:00", "Europe/Paris",      "2008-06-30T11:05", null},
                {2008, 6, 30, 11, 5, 30, null, "+01:00", "Europe/Paris",        "2008-06-30T11:05:30", null},
                {2008, 6, 30, 11, 5, 30, 500000000, "+01:00", "Europe/Paris",   "2008-06-30T11:05:30.5", null},
                {2008, 6, 30, 11, 5, 30, 1, "+01:00", "Europe/Paris",           "2008-06-30T11:05:30.000000001", null},

                {2008, 6, 30, 11, 5, null, null, null, "Europe/Paris",          "2008-06-30T11:05", null},
                {2008, 6, 30, 11, 5, 30, null, null, "Europe/Paris",            "2008-06-30T11:05:30", null},
                {2008, 6, 30, 11, 5, 30, 500000000, null, "Europe/Paris",       "2008-06-30T11:05:30.5", null},
                {2008, 6, 30, 11, 5, 30, 1, null, "Europe/Paris",               "2008-06-30T11:05:30.000000001", null},

                {123456, 6, 30, 11, 5, null, null, null, null,                  "+123456-06-30T11:05", null},
        };
    }

    @Test(dataProvider="sample_isoLocalDateTime", groups={"tck"})
    public void test_print_isoLocalDateTime(
            Integer year, Integer month, Integer day,
            Integer hour, Integer min, Integer sec, Integer nano, String offsetId, String zoneId,
            String expected, Class<?> expectedEx) {
        DateTimeBuilder test = createDateTime(year, month, day, hour, min, sec, nano);
        buildCalendrical(test, offsetId, zoneId);
        if (expectedEx == null) {
            assertEquals(DateTimeFormatters.isoLocalDateTime().print(test), expected);
        } else {
            try {
                DateTimeFormatters.isoLocalDateTime().print(test);
                fail();
            } catch (Exception ex) {
                assertTrue(expectedEx.isInstance(ex));
            }
        }
    }

    @Test(dataProvider="sample_isoLocalDateTime", groups={"tck"})
    public void test_parse_isoLocalDateTime(
            Integer year, Integer month, Integer day,
            Integer hour, Integer min, Integer sec, Integer nano, String offsetId, String zoneId,
            String input, Class<?> invalid) {
        if (input != null) {
            DateTimeBuilder expected = createDateTime(year, month, day, hour, min, sec, nano);
            assertParseMatch(DateTimeFormatters.isoLocalDateTime().parseToBuilder(input, new ParsePosition(0)), expected);
        }
    }

    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    @DataProvider(name="sample_isoOffsetDateTime")
    Object[][] provider_sample_isoOffsetDateTime() {
        return new Object[][]{
                {2008, null, null, null, null, null, null, null, null, null, DateTimeException.class},
                {null, 6, null, null, null, null, null, null, null, null, DateTimeException.class},
                {null, null, 30, null, null, null, null, null, null, null, DateTimeException.class},
                {null, null, null, 11, null, null, null, null, null, null, DateTimeException.class},
                {null, null, null, null, 5, null, null, null, null, null, DateTimeException.class},
                {null, null, null, null, null, null, null, "+01:00", null, null, DateTimeException.class},
                {null, null, null, null, null, null, null, null, "Europe/Paris", null, DateTimeException.class},
                {2008, 6, 30, 11, null, null, null, null, null, null, DateTimeException.class},
                {2008, 6, 30, null, 5, null, null, null, null, null, DateTimeException.class},
                {2008, 6, null, 11, 5, null, null, null, null, null, DateTimeException.class},
                {2008, null, 30, 11, 5, null, null, null, null, null, DateTimeException.class},
                {null, 6, 30, 11, 5, null, null, null, null, null, DateTimeException.class},

                {2008, 6, 30, 11, 5, null, null, null, null,                    null, DateTimeException.class},
                {2008, 6, 30, 11, 5, 30, null, null, null,                      null, DateTimeException.class},
                {2008, 6, 30, 11, 5, 30, 500000000, null, null,                 null, DateTimeException.class},
                {2008, 6, 30, 11, 5, 30, 1, null, null,                         null, DateTimeException.class},

                {2008, 6, 30, 11, 5, null, null, "+01:00", null,                "2008-06-30T11:05+01:00", null},
                {2008, 6, 30, 11, 5, 30, null, "+01:00", null,                  "2008-06-30T11:05:30+01:00", null},
                {2008, 6, 30, 11, 5, 30, 500000000, "+01:00", null,             "2008-06-30T11:05:30.5+01:00", null},
                {2008, 6, 30, 11, 5, 30, 1, "+01:00", null,                     "2008-06-30T11:05:30.000000001+01:00", null},

                {2008, 6, 30, 11, 5, null, null, "+01:00", "Europe/Paris",      "2008-06-30T11:05+01:00", null},
                {2008, 6, 30, 11, 5, 30, null, "+01:00", "Europe/Paris",        "2008-06-30T11:05:30+01:00", null},
                {2008, 6, 30, 11, 5, 30, 500000000, "+01:00", "Europe/Paris",   "2008-06-30T11:05:30.5+01:00", null},
                {2008, 6, 30, 11, 5, 30, 1, "+01:00", "Europe/Paris",           "2008-06-30T11:05:30.000000001+01:00", null},

                {2008, 6, 30, 11, 5, null, null, null, "Europe/Paris",          null, DateTimeException.class},
                {2008, 6, 30, 11, 5, 30, null, null, "Europe/Paris",            null, DateTimeException.class},
                {2008, 6, 30, 11, 5, 30, 500000000, null, "Europe/Paris",       null, DateTimeException.class},
                {2008, 6, 30, 11, 5, 30, 1, null, "Europe/Paris",               null, DateTimeException.class},

                {123456, 6, 30, 11, 5, null, null, "+01:00", null,              "+123456-06-30T11:05+01:00", null},
        };
    }

    @Test(dataProvider="sample_isoOffsetDateTime", groups={"tck"})
    public void test_print_isoOffsetDateTime(
            Integer year, Integer month, Integer day,
            Integer hour, Integer min, Integer sec, Integer nano, String offsetId, String zoneId,
            String expected, Class<?> expectedEx) {
        DateTimeBuilder test = createDateTime(year, month, day, hour, min, sec, nano);
        buildCalendrical(test, offsetId, zoneId);
        if (expectedEx == null) {
            assertEquals(DateTimeFormatters.isoOffsetDateTime().print(test), expected);
        } else {
            try {
                DateTimeFormatters.isoOffsetDateTime().print(test);
                fail();
            } catch (Exception ex) {
                assertTrue(expectedEx.isInstance(ex));
            }
        }
    }

    @Test(dataProvider="sample_isoOffsetDateTime", groups={"tck"})
    public void test_parse_isoOffsetDateTime(
            Integer year, Integer month, Integer day,
            Integer hour, Integer min, Integer sec, Integer nano, String offsetId, String zoneId,
            String input, Class<?> invalid) {
        if (input != null) {
            DateTimeBuilder expected = createDateTime(year, month, day, hour, min, sec, nano);
            buildCalendrical(expected, offsetId, null);  // zone not expected to be parsed
            assertParseMatch(DateTimeFormatters.isoOffsetDateTime().parseToBuilder(input, new ParsePosition(0)), expected);
        }
    }

    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    @DataProvider(name="sample_isoZonedDateTime")
    Object[][] provider_sample_isoZonedDateTime() {
        return new Object[][]{
                {2008, null, null, null, null, null, null, null, null, null, DateTimeException.class},
                {null, 6, null, null, null, null, null, null, null, null, DateTimeException.class},
                {null, null, 30, null, null, null, null, null, null, null, DateTimeException.class},
                {null, null, null, 11, null, null, null, null, null, null, DateTimeException.class},
                {null, null, null, null, 5, null, null, null, null, null, DateTimeException.class},
                {null, null, null, null, null, null, null, "+01:00", null, null, DateTimeException.class},
                {null, null, null, null, null, null, null, null, "Europe/Paris", null, DateTimeException.class},
                {2008, 6, 30, 11, null, null, null, null, null, null, DateTimeException.class},
                {2008, 6, 30, null, 5, null, null, null, null, null, DateTimeException.class},
                {2008, 6, null, 11, 5, null, null, null, null, null, DateTimeException.class},
                {2008, null, 30, 11, 5, null, null, null, null, null, DateTimeException.class},
                {null, 6, 30, 11, 5, null, null, null, null, null, DateTimeException.class},

                {2008, 6, 30, 11, 5, null, null, null, null,                    null, DateTimeException.class},
                {2008, 6, 30, 11, 5, 30, null, null, null,                      null, DateTimeException.class},
                {2008, 6, 30, 11, 5, 30, 500000000, null, null,                 null, DateTimeException.class},
                {2008, 6, 30, 11, 5, 30, 1, null, null,                         null, DateTimeException.class},

                {2008, 6, 30, 11, 5, null, null, "+01:00", null,                null, DateTimeException.class},
                {2008, 6, 30, 11, 5, 30, null, "+01:00", null,                  null, DateTimeException.class},
                {2008, 6, 30, 11, 5, 30, 500000000, "+01:00", null,             null, DateTimeException.class},
                {2008, 6, 30, 11, 5, 30, 1, "+01:00", null,                     null, DateTimeException.class},

                {2008, 6, 30, 11, 5, null, null, "+01:00", "Europe/Paris",      "2008-06-30T11:05+01:00[Europe/Paris]", null},
                {2008, 6, 30, 11, 5, 30, null, "+01:00", "Europe/Paris",        "2008-06-30T11:05:30+01:00[Europe/Paris]", null},
                {2008, 6, 30, 11, 5, 30, 500000000, "+01:00", "Europe/Paris",   "2008-06-30T11:05:30.5+01:00[Europe/Paris]", null},
                {2008, 6, 30, 11, 5, 30, 1, "+01:00", "Europe/Paris",           "2008-06-30T11:05:30.000000001+01:00[Europe/Paris]", null},

                {2008, 6, 30, 11, 5, null, null, null, "Europe/Paris",          null, DateTimeException.class},
                {2008, 6, 30, 11, 5, 30, null, null, "Europe/Paris",            null, DateTimeException.class},
                {2008, 6, 30, 11, 5, 30, 500000000, null, "Europe/Paris",       null, DateTimeException.class},
                {2008, 6, 30, 11, 5, 30, 1, null, "Europe/Paris",               null, DateTimeException.class},

                {123456, 6, 30, 11, 5, null, null, "+01:00", "Europe/Paris",    "+123456-06-30T11:05+01:00[Europe/Paris]", null},
        };
    }

    @Test(dataProvider="sample_isoZonedDateTime", groups={"tck"})
    public void test_print_isoZonedDateTime(
            Integer year, Integer month, Integer day,
            Integer hour, Integer min, Integer sec, Integer nano, String offsetId, String zoneId,
            String expected, Class<?> expectedEx) {
        DateTimeBuilder test = createDateTime(year, month, day, hour, min, sec, nano);
        buildCalendrical(test, offsetId, zoneId);
        if (expectedEx == null) {
            assertEquals(DateTimeFormatters.isoZonedDateTime().print(test), expected);
        } else {
            try {
                DateTimeFormatters.isoZonedDateTime().print(test);
                fail();
            } catch (Exception ex) {
                assertTrue(expectedEx.isInstance(ex));
            }
        }
    }

    @Test(dataProvider="sample_isoZonedDateTime", groups={"tck"})
    public void test_parse_isoZonedDateTime(
            Integer year, Integer month, Integer day,
            Integer hour, Integer min, Integer sec, Integer nano, String offsetId, String zoneId,
            String input, Class<?> invalid) {
        if (input != null) {
            DateTimeBuilder expected = createDateTime(year, month, day, hour, min, sec, nano);
            buildCalendrical(expected, offsetId, zoneId);
            assertParseMatch(DateTimeFormatters.isoZonedDateTime().parseToBuilder(input, new ParsePosition(0)), expected);
        }
    }

    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    @DataProvider(name="sample_isoDateTime")
    Object[][] provider_sample_isoDateTime() {
        return new Object[][]{
                {2008, null, null, null, null, null, null, null, null, null, DateTimeException.class},
                {null, 6, null, null, null, null, null, null, null, null, DateTimeException.class},
                {null, null, 30, null, null, null, null, null, null, null, DateTimeException.class},
                {null, null, null, 11, null, null, null, null, null, null, DateTimeException.class},
                {null, null, null, null, 5, null, null, null, null, null, DateTimeException.class},
                {null, null, null, null, null, null, null, "+01:00", null, null, DateTimeException.class},
                {null, null, null, null, null, null, null, null, "Europe/Paris", null, DateTimeException.class},
                {2008, 6, 30, 11, null, null, null, null, null, null, DateTimeException.class},
                {2008, 6, 30, null, 5, null, null, null, null, null, DateTimeException.class},
                {2008, 6, null, 11, 5, null, null, null, null, null, DateTimeException.class},
                {2008, null, 30, 11, 5, null, null, null, null, null, DateTimeException.class},
                {null, 6, 30, 11, 5, null, null, null, null, null, DateTimeException.class},

                {2008, 6, 30, 11, 5, null, null, null, null,                    "2008-06-30T11:05", null},
                {2008, 6, 30, 11, 5, 30, null, null, null,                      "2008-06-30T11:05:30", null},
                {2008, 6, 30, 11, 5, 30, 500000000, null, null,                 "2008-06-30T11:05:30.5", null},
                {2008, 6, 30, 11, 5, 30, 1, null, null,                         "2008-06-30T11:05:30.000000001", null},

                {2008, 6, 30, 11, 5, null, null, "+01:00", null,                "2008-06-30T11:05+01:00", null},
                {2008, 6, 30, 11, 5, 30, null, "+01:00", null,                  "2008-06-30T11:05:30+01:00", null},
                {2008, 6, 30, 11, 5, 30, 500000000, "+01:00", null,             "2008-06-30T11:05:30.5+01:00", null},
                {2008, 6, 30, 11, 5, 30, 1, "+01:00", null,                     "2008-06-30T11:05:30.000000001+01:00", null},

                {2008, 6, 30, 11, 5, null, null, "+01:00", "Europe/Paris",      "2008-06-30T11:05+01:00[Europe/Paris]", null},
                {2008, 6, 30, 11, 5, 30, null, "+01:00", "Europe/Paris",        "2008-06-30T11:05:30+01:00[Europe/Paris]", null},
                {2008, 6, 30, 11, 5, 30, 500000000, "+01:00", "Europe/Paris",   "2008-06-30T11:05:30.5+01:00[Europe/Paris]", null},
                {2008, 6, 30, 11, 5, 30, 1, "+01:00", "Europe/Paris",           "2008-06-30T11:05:30.000000001+01:00[Europe/Paris]", null},

                {2008, 6, 30, 11, 5, null, null, null, "Europe/Paris",          "2008-06-30T11:05", null},
                {2008, 6, 30, 11, 5, 30, null, null, "Europe/Paris",            "2008-06-30T11:05:30", null},
                {2008, 6, 30, 11, 5, 30, 500000000, null, "Europe/Paris",       "2008-06-30T11:05:30.5", null},
                {2008, 6, 30, 11, 5, 30, 1, null, "Europe/Paris",               "2008-06-30T11:05:30.000000001", null},

                {123456, 6, 30, 11, 5, null, null, null, null,                  "+123456-06-30T11:05", null},
        };
    }

    @Test(dataProvider="sample_isoDateTime", groups={"tck"})
    public void test_print_isoDateTime(
            Integer year, Integer month, Integer day,
            Integer hour, Integer min, Integer sec, Integer nano, String offsetId, String zoneId,
            String expected, Class<?> expectedEx) {
        DateTimeBuilder test = createDateTime(year, month, day, hour, min, sec, nano);
        buildCalendrical(test, offsetId, zoneId);
        if (expectedEx == null) {
            assertEquals(DateTimeFormatters.isoDateTime().print(test), expected);
        } else {
            try {
                DateTimeFormatters.isoDateTime().print(test);
                fail();
            } catch (Exception ex) {
                assertTrue(expectedEx.isInstance(ex));
            }
        }
    }

    @Test(dataProvider="sample_isoDateTime", groups={"tck"})
    public void test_parse_isoDateTime(
            Integer year, Integer month, Integer day,
            Integer hour, Integer min, Integer sec, Integer nano, String offsetId, String zoneId,
            String input, Class<?> invalid) {
        if (input != null) {
            DateTimeBuilder expected = createDateTime(year, month, day, hour, min, sec, nano);
            if (offsetId != null) {
                expected.addCalendrical(ZoneOffset.of(offsetId));
                if (zoneId != null) {
                    expected.addCalendrical(ZoneId.of(zoneId));
                }
            }
            assertParseMatch(DateTimeFormatters.isoDateTime().parseToBuilder(input, new ParsePosition(0)), expected);
        }
    }

    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_print_isoOrdinalDate() {
        DateTimeAccessor test = LocalDateTime.of(2008, 6, 3, 11, 5, 30);
        assertEquals(DateTimeFormatters.isoOrdinalDate().print(test), "2008-155");
    }

    @Test(groups={"tck"})
    public void test_print_isoOrdinalDate_offset() {
        DateTimeAccessor test = OffsetDateTime.of(2008, 6, 3, 11, 5, 30, ZoneOffset.UTC);
        assertEquals(DateTimeFormatters.isoOrdinalDate().print(test), "2008-155Z");
    }

    @Test(groups={"tck"})
    public void test_print_isoOrdinalDate_zoned() {
        DateTimeAccessor test = ZonedDateTime.of(LocalDateTime.of(2008, 6, 3, 11, 5, 30), ZoneId.UTC);
        assertEquals(DateTimeFormatters.isoOrdinalDate().print(test), "2008-155Z[UTC:Z]");
    }

    @Test(groups={"tck"})
    public void test_print_isoOrdinalDate_zoned_largeYear() {
        DateTimeAccessor test = ZonedDateTime.of(LocalDateTime.of(123456, 6, 3, 11, 5, 30), ZoneId.UTC);
        assertEquals(DateTimeFormatters.isoOrdinalDate().print(test), "+123456-155Z[UTC:Z]");
    }

    @Test(groups={"tck"})
    public void test_print_isoOrdinalDate_fields() {
        DateTimeAccessor test = new DateTimeBuilder(YEAR, 2008).addFieldValue(DAY_OF_YEAR, 231);
        assertEquals(DateTimeFormatters.isoOrdinalDate().print(test), "2008-231");
    }

    @Test(expectedExceptions=DateTimeException.class, groups={"tck"})
    public void test_print_isoOrdinalDate_missingField() {
        DateTimeAccessor test = Year.of(2008);
        DateTimeFormatters.isoOrdinalDate().print(test);
    }

    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_parse_isoOrdinalDate() {
        DateTimeBuilder expected = new DateTimeBuilder(YEAR, 2008).addFieldValue(DAY_OF_YEAR, 123);
        assertParseMatch(DateTimeFormatters.isoOrdinalDate().parseToBuilder("2008-123", new ParsePosition(0)), expected);
    }

    @Test(groups={"tck"})
    public void test_parse_isoOrdinalDate_largeYear() {
        DateTimeBuilder expected = new DateTimeBuilder(YEAR, 123456).addFieldValue(DAY_OF_YEAR, 123);
        assertParseMatch(DateTimeFormatters.isoOrdinalDate().parseToBuilder("+123456-123", new ParsePosition(0)), expected);
    }

    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_print_basicIsoDate() {
        DateTimeAccessor test = LocalDateTime.of(2008, 6, 3, 11, 5, 30);
        assertEquals(DateTimeFormatters.basicIsoDate().print(test), "20080603");
    }

    @Test(groups={"tck"})
    public void test_print_basicIsoDate_offset() {
        DateTimeAccessor test = OffsetDateTime.of(2008, 6, 3, 11, 5, 30, ZoneOffset.UTC);
        assertEquals(DateTimeFormatters.basicIsoDate().print(test), "20080603Z");
    }

    @Test(groups={"tck"})
    public void test_print_basicIsoDate_zoned() {
        DateTimeAccessor test = ZonedDateTime.of(LocalDateTime.of(2008, 6, 3, 11, 5, 30), ZoneId.UTC);
        assertEquals(DateTimeFormatters.basicIsoDate().print(test), "20080603Z[UTC:Z]");
    }

    @Test(expectedExceptions=DateTimePrintException.class, groups={"tck"})
    public void test_print_basicIsoDate_largeYear() {
        DateTimeAccessor test = ZonedDateTime.of(LocalDateTime.of(123456, 6, 3, 11, 5, 30), ZoneId.UTC);
        DateTimeFormatters.basicIsoDate().print(test);
    }

    @Test(groups={"tck"})
    public void test_print_basicIsoDate_fields() {
        DateTimeAccessor test = LocalDate.of(2008, 6, 30);
        assertEquals(DateTimeFormatters.basicIsoDate().print(test), "20080630");
    }

    @Test(expectedExceptions=DateTimeException.class, groups={"tck"})
    public void test_print_basicIsoDate_missingField() {
        DateTimeAccessor test = YearMonth.of(2008, 6);
        DateTimeFormatters.basicIsoDate().print(test);
    }

    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_parse_basicIsoDate() {
        LocalDate expected = LocalDate.of(2008, 6, 3);
        assertEquals(DateTimeFormatters.basicIsoDate().parse("20080603", LocalDate.class), expected);
    }

    @Test(expectedExceptions=DateTimeParseException.class, groups={"tck"})
    public void test_parse_basicIsoDate_largeYear() {
        try {
            LocalDate expected = LocalDate.of(123456, 6, 3);
            assertEquals(DateTimeFormatters.basicIsoDate().parse("+1234560603", LocalDate.class), expected);
        } catch (DateTimeParseException ex) {
            assertEquals(ex.getErrorIndex(), 0);
            assertEquals(ex.getParsedString(), "+1234560603");
            throw ex;
        }
    }

    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    @DataProvider(name="weekDate")
    Iterator<Object[]> weekDate() {
        return new Iterator<Object[]>() {
            private ZonedDateTime date = ZonedDateTime.of(LocalDateTime.of(2003, 12, 29, 11, 5, 30), ZoneId.UTC);
            private ZonedDateTime endDate = date.withDate(2005, 1, 2);
            private int week = 1;
            private int day = 1;

            public boolean hasNext() {
                return !date.isAfter(endDate);
            }
            public Object[] next() {
                StringBuilder sb = new StringBuilder("2004-W");
                if (week < 10) {
                    sb.append('0');
                }
                sb.append(week).append('-').append(day).append("Z[UTC:Z]");
                Object[] ret = new Object[] {date, sb.toString()};
                date = date.plusDays(1);
                day += 1;
                if (day == 8) {
                    day = 1;
                    week++;
                }
                return ret;
            }
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Test(dataProvider="weekDate", groups={"tck"})
    public void test_print_isoWeekDate(DateTimeAccessor test, String expected) {
        assertEquals(DateTimeFormatters.isoWeekDate().print(test), expected);
    }

    @Test(groups={"tck"})
    public void test_print_isoWeekDate_zoned_largeYear() {
        DateTimeAccessor test = ZonedDateTime.of(LocalDateTime.of(123456, 6, 3, 11, 5, 30), ZoneId.UTC);
        assertEquals(DateTimeFormatters.isoWeekDate().print(test), "+123456-W23-2Z[UTC:Z]");
    }

    @Test(groups={"tck"})
    public void test_print_isoWeekDate_fields() {
        DateTimeAccessor test = LocalDate.of(2004, 1, 27);
        assertEquals(DateTimeFormatters.isoWeekDate().print(test), "2004-W05-2");
    }

    @Test(expectedExceptions=DateTimeException.class, groups={"tck"})
    public void test_print_isoWeekDate_missingField() {
        DateTimeAccessor test = YearMonth.of(2008, 6);
        DateTimeFormatters.isoWeekDate().print(test);
    }

    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_parse_weekDate() {
        LocalDate expected = LocalDate.of(2004, 1, 28);
        assertEquals(DateTimeFormatters.isoWeekDate().parse("2004-W05-3", LocalDate.class), expected);
    }

    @Test(groups={"tck"})
    public void test_parse_weekDate_largeYear() {
        DateTimeBuilder builder = DateTimeFormatters.isoWeekDate().parseToBuilder("+123456-W04-5", new ParsePosition(0));
        assertEquals(builder.getFieldValue(ISOWeeks.WEEK_BASED_YEAR), 123456);
        assertEquals(builder.getFieldValue(ISOWeeks.WEEK_OF_WEEK_BASED_YEAR), 4);
        assertEquals(builder.getFieldValue(DAY_OF_WEEK), 5);
    }

    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    @Test(groups={"tck"})
    public void test_print_rfc1123() {
        DateTimeAccessor test = ZonedDateTime.of(LocalDateTime.of(2008, 6, 3, 11, 5, 30), ZoneId.UTC);
        assertEquals(DateTimeFormatters.rfc1123().print(test), "Tue, 03 Jun 2008 11:05:30 Z");
    }

    @Test(groups={"tck"}, expectedExceptions=DateTimeException.class)
    public void test_print_rfc1123_missingField() {
        DateTimeAccessor test = YearMonth.of(2008, 6);
        DateTimeFormatters.rfc1123().print(test);
    }

    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    private DateTimeBuilder createDate(Integer year, Integer month, Integer day) {
        DateTimeBuilder test = new DateTimeBuilder();
        if (year != null) {
            test.addFieldValue(YEAR, year);
        }
        if (month != null) {
            test.addFieldValue(MONTH_OF_YEAR, month);
        }
        if (day != null) {
            test.addFieldValue(DAY_OF_MONTH, day);
        }
        return test;
    }

    private DateTimeBuilder createTime(Integer hour, Integer min, Integer sec, Integer nano) {
        DateTimeBuilder test = new DateTimeBuilder();
        if (hour != null) {
            test.addFieldValue(HOUR_OF_DAY, hour);
        }
        if (min != null) {
            test.addFieldValue(MINUTE_OF_HOUR, min);
        }
        if (sec != null) {
            test.addFieldValue(SECOND_OF_MINUTE, sec);
        }
        if (nano != null) {
            test.addFieldValue(NANO_OF_SECOND, nano);
        }
        return test;
    }

    private DateTimeBuilder createDateTime(
            Integer year, Integer month, Integer day,
            Integer hour, Integer min, Integer sec, Integer nano) {
        DateTimeBuilder test = new DateTimeBuilder();
        if (year != null) {
            test.addFieldValue(YEAR, year);
        }
        if (month != null) {
            test.addFieldValue(MONTH_OF_YEAR, month);
        }
        if (day != null) {
            test.addFieldValue(DAY_OF_MONTH, day);
        }
        if (hour != null) {
            test.addFieldValue(HOUR_OF_DAY, hour);
        }
        if (min != null) {
            test.addFieldValue(MINUTE_OF_HOUR, min);
        }
        if (sec != null) {
            test.addFieldValue(SECOND_OF_MINUTE, sec);
        }
        if (nano != null) {
            test.addFieldValue(NANO_OF_SECOND, nano);
        }
        return test;
    }

    private void buildCalendrical(DateTimeBuilder cal, String offsetId, String zoneId) {
        if (offsetId != null) {
            cal.addCalendrical(ZoneOffset.of(offsetId));
        }
        if (zoneId != null) {
            cal.addCalendrical(ZoneId.of(zoneId));
        }
    }

    private void assertParseMatch(DateTimeBuilder parsed, DateTimeBuilder expected) {
        Map<DateTimeField, Long> parsedFVMap = parsed.getFieldValueMap();
        Map<DateTimeField, Long> expectedFVMap = expected.getFieldValueMap();
        assertEquals(parsedFVMap, expectedFVMap);

        List<Object> parsedCMap = parsed.getCalendricalList();
        List<Object> expectedCMap = expected.getCalendricalList();
        assertEquals(parsedCMap, expectedCMap);
    }

}
