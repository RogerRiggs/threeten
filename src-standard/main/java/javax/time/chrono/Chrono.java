/*
 * Copyright (c) 2012, Stephen Colebourne & Michael Nascimento Santos
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
package javax.time.chrono;

import java.util.Locale;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.time.LocalDate;
import javax.time.calendrical.CalendricalObject;

/**
 * Factory interface for calendar neutral {@code ChronoDate}s.
 * The Chrono for a particular calendar is found using the lookup methods
 * {@link Chrono#getByName} and {@link Chrono#getByLocale(java.util.Locale)}.
 * New calendars can be added by providing implementations of {@code Chrono}
 * as described in the <a href="#addcalendars">Adding New Calendars</a> section below.
 * <p>
 * The lookup of calendars is supported by the {@link Chrono#getByName Chrono.getByName(name)}
 * and {@link Chrono#getByLocale Chrono.getByLocale(locale)} methods.
 * The {@code Chrono} is then used to create a {@code ChronoDate}
 * from year, month, day, another Calendrical, or the current date.
 * The names of available calendars are available via the
 * {@link Chrono#getCalendarNames Chrono.getCalendarNames} method.
 * <p>
 * ChronoDate instances are created using the methods:
 * <ul>
 * <li> {@link #now() now()},
 * <li> {@link #date(int, int, int) date(year, month, day)},
 * <li> {@link #date(javax.time.chrono.Era, int, int, int) date(era, year, month, day)},
 * <li> {@link #date(javax.time.calendrical.CalendricalObject) date(Calendrical)}.
 * <li> {@link #dateFromEpochDay(long) dateFromEpochDay(epochDay)}, or
 * </ul>
 *
 * <h4 id="addcalendars">Adding New Calendars</h4>
 *
 * <p>
 * The set of calendars is extensible by defining a subclass of {@link javax.time.chrono.ChronoDate}
 * to represent a date instance and an implementation of {@link javax.time.chrono.Chrono}
 * to be the factory for the ChronoDate subclass.
 * The {@link java.util.ServiceLoader} mechanism is used to register
 * the Chrono subclass class. The calendar lookup mechanism
 * uses the ServiceLoader to find Chrono implementations
 * and registers them by name and locale.
 * </p>

 * <p>Example getting the current date for a named calendar</p>
 * <pre>
 *     Chrono chrono = Chrono.getByName(CLDR_name);
 *     ChronoDate date = chrono.now();
 * </pre>
 * @see ChronoDate
 */
public abstract class Chrono {

    /*
     * Initialize the available calendars.
     * TBD: The initialization should be deferred until some application requests
     * a specific calendar.
     */
    static {
        chronos = new ConcurrentHashMap<String, Chrono>();
        initCalendars();
    }
    /**
     * The global map of available calendars; mapped from calendar name to Chrono.
     */
    private static final Map<String, Chrono> chronos;

    /**
     * Accumulate all of the Chrono implementations from the built-in ones
     * and the built-in implementations.
     */
    private static void initCalendars() {
        // Pre-register well known calendars
        chronos.put(ISOChrono.INSTANCE.getName(), ISOChrono.INSTANCE);
        chronos.put(CopticChrono.INSTANCE.getName(), CopticChrono.INSTANCE);
        chronos.put(MinguoChrono.INSTANCE.getName(), MinguoChrono.INSTANCE);

        ServiceLoader<Chrono> loader =  ServiceLoader.load(Chrono.class);
        for (Chrono chrono : loader) {
            chronos.put(chrono.getName(), chrono);
        }
    }

    /**
     * Returns the calendar for the locale.
     *
     * @param locale The Locale
     * @return A calendar for the Locale
     * @throws UnsupportedOperationException if a calendar for the locale cannot be found.
     */
    public static Chrono getByLocale(Locale locale) {
        throw new UnsupportedOperationException("NYI: Chrono.getByLocale");
    }

    /**
     * Returns the calendar by name.
     * @param calendar The calendar name
     * @return A calendar with the name requested.
     * @throws UnsupportedOperationException if the named calendar cannot be found.
     */
    public static Chrono getByName(String calendar) {
        Chrono chrono = chronos.get(calendar);
        if (chrono == null) {
            throw new UnsupportedOperationException("No calendar for: " + calendar);
        }
        return chrono;
    }

    /**
     * Returns the names of available calendars.
     * @return  the set of available calendar names.
     */
    public static Set<String> getCalendarNames() {
        return chronos.keySet();
    }

    /**
     * Gets the name of the calendar system.
     * 
     * @return the name, not null
     */
    public abstract String getName();

    //-----------------------------------------------------------------------
    /**
     * Creates a date in this calendar system from the era, year-of-era, month-of-year and day-of-month fields.
     * 
     * @param era  the calendar system era of the correct type, not null
     * @param yearOfEra  the calendar system year-of-era
     * @param monthOfYear  the calendar system month-of-year
     * @param dayOfMonth  the calendar system day-of-month
     * @return the date in this calendar system, not null
     */
    public abstract ChronoDate date(Era era, int yearOfEra, int monthOfYear, int dayOfMonth);

    /**
     * Creates a date in this calendar system from the proleptic-year, month-of-year and day-of-month fields.
     * 
     * @param prolepticYear  the calendar system proleptic-year
     * @param monthOfYear  the calendar system month-of-year
     * @param dayOfMonth  the calendar system day-of-month
     * @return the date in this calendar system, not null
     */
    public abstract ChronoDate date(int prolepticYear, int monthOfYear, int dayOfMonth);

    /**
     * Creates a date in this calendar system from another calendrical object.
     * 
     * @param calendrical  the other calendrical, not null
     * @return the date in this calendar system, not null
     */
    public abstract ChronoDate date(CalendricalObject calendrical);

    /**
     * Creates a date in this calendar system from the epoch day from 1970-01-01 (ISO).
     * 
     * @param epochDay  the epoch day measured from 1970-01-01 (ISO), not null
     * @return the date in this calendar system, not null
     */
    public abstract ChronoDate dateFromEpochDay(long epochDay);

    /**
     * Creates the current date in this calendar system.
     * 
     * @return the current date in this calendar system, not null
     */
    public ChronoDate now() {
        return dateFromEpochDay(LocalDate.now().toEpochDay());
    }

    /**
     * Checks if the specified year is a leap year.
     * <p>
     * A leap-year is a year of a longer length than normal.
     * The exact meaning is determined by the chronology according to the following constraints.
     * <p>
     * A leap-year must imply a year-length longer than a non leap-year.
     *
     * @param prolepticYear  the proleptic-year to check, not validated for range
     * @return true if the year is a leap year
     */
    public abstract boolean isLeapYear(long prolepticYear);

    //-----------------------------------------------------------------------
    /**
     * Creates the calendar system era object from the numeric value.
     * <p>
     * The era is, conceptually, the largest division of the time-line.
     * Most calendar systems have a single epoch dividing the time-line into two eras.
     * However, some have multiple eras, such as one for the reign of each leader.
     * The exact meaning is determined by the chronology according to the following constraints.
     * <p>
     * The era in use at 1970-01-01 must have the value 1.
     * Later eras must have sequentially higher values.
     * Earlier eras must have sequentially lower values.
     * Each chronology must refer to an enum or similar singleton to provide the era values.
     * <p>
     * This method returns the singleton era of the correct type for the specified era value.
     *
     * @param eraValue  the era value
     * @return the calendar system era, not null
     */
    public abstract Era createEra(int eraValue);

}
