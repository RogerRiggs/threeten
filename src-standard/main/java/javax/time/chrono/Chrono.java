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

import javax.time.calendrical.CalendricalObject;

/**
 * Factory interface for calendar neutral {@code ChronoDate}s.
 * The Chrono for a particular calendars is found using the lookup methods
 * {@link ChronoDate#getByName} and {@link ChronoDate#getByLocale(java.util.Locale)}.
 * New calendars can be added by providing implementations of the {@code Chrono} interface
 * as described in the Adding New Calendars section below.
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
 * <h4>Adding New Calendars</h4>
 *
 * <p>
 * The set of calendars is extensible by defining a subclass of {@link javax.time.chrono.ChronoDate}
 * to represent a date instance and an implementation of {@link javax.time.chrono.Chrono}
 * to be the factory for the ChronoDate subclass.
 * The {@link java.util.ServiceLoader} mechanism is used to register
 * the Chrono implementation class. The calendar lookup mechanism in
 * {@link ChronoDate} uses the ServiceLoader to find Chrono implementations
 * and registers them by name and locale.
 * </p>

 * <p>Example getting the current date for a named calendar</p>
 * <pre>
 *     Chrono chrono = ChronoDate.getByName(CLDR_name);
 *     ChronoDate date = chrono.now();
 * </pre>
 * @see ChronoDate
 */
public interface Chrono {

    /**
     * Gets the name of the calendar system.
     * 
     * @return the name, not null
     */
    String getName();

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
    ChronoDate date(Era era, int yearOfEra, int monthOfYear, int dayOfMonth);

    /**
     * Creates a date in this calendar system from the proleptic-year, month-of-year and day-of-month fields.
     * 
     * @param prolepticYear  the calendar system proleptic-year
     * @param monthOfYear  the calendar system month-of-year
     * @param dayOfMonth  the calendar system day-of-month
     * @return the date in this calendar system, not null
     */
    ChronoDate date(int prolepticYear, int monthOfYear, int dayOfMonth);

    /**
     * Creates a date in this calendar system from another calendrical object.
     * 
     * @param calendrical  the other calendrical, not null
     * @return the date in this calendar system, not null
     */
    ChronoDate date(CalendricalObject calendrical);

    /**
     * Creates a date in this calendar system from the epoch day from 1970-01-01 (ISO).
     * 
     * @param epochDay  the epoch day measured from 1970-01-01 (ISO), not null
     * @return the date in this calendar system, not null
     */
    ChronoDate dateFromEpochDay(long epochDay);

    /**
     * Creates the current date in this calendar system.
     * 
     * @return the current date in this calendar system, not null
     */
    // This is a candidate for an SE 8 default method calling dateFromEpochDay()
    ChronoDate now();

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
    boolean isLeapYear(long prolepticYear);

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
    Era createEra(int eraValue);

}
