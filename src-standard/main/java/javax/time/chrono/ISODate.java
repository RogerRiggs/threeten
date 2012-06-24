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
 * PCEUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package javax.time.chrono;

import java.io.Serializable;

import javax.time.CalendricalException;
import javax.time.LocalDate;
import javax.time.calendrical.DateTimeField;
import javax.time.calendrical.LocalDateTimeField;

/**
 * A date in the ISO calendar system.
 * <p>
 * This class is intended for applications that need to use a calendar system other than
 * ISO-8601, the <i>de facto</i> world calendar.
 * <p>
 * This class is limited to storing a date, using the generic concepts of year, month and day.
 * For example, the Mayan calendar uses a system that bears no relation to years, months and days.
 * <p>
 * The fields of ISODate are defined as follows:
 * <ul>
 * <li>era - There are two eras, the 'Current Era' (CE) and 'Before Current Era' (BCE).
 * <li>year-of-era - The year-of-era is the same as the proleptic-year for the current CE era.
 * <li>proleptic-year - The proleptic year is the same as the year-of-era for the
 *  current CE era. For the BCE era, years have negative values.
 * <li>month-of-year - There are 12 months in an ISO year, numbered from 1 to 12.
 * <li>day-of-month - There are between 28 and 31 days in each of the ISO month, numbered from 1 to 31.
 *  Months 4, 6, 9 and 11 have 30 days, Months 1, 3, 5, 7, 8, 10 and 12 have 31 days.
 *  Month 2 has 28 days, or 29 in a leap year.
 * <li>day-of-year - There are 365 days in a standard ISO year and 366 in a leap year.
 *  The days are numbered from 1 to 365 or 1 to 366.
 * <li>leap-year - Leap years occur every 4 years, except where the year is divisble by 100 and not divisble by 400.
 * </ul>
 *
 * <h4>Implementation notes</h4>
 * This class is immutable and thread-safe.
 */
final class ISODate extends ChronoDate implements Comparable<ChronoDate>, Serializable {
    // this class is package-scoped so that future conversion to public
    // would not change serialization

    /**
     * Serialization version.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The ISO date.
     */
    private final LocalDate isoDate;

    /**
     * Creates an instance.
     * 
     * @param date  the equivalent ISO date
     */
    ISODate(LocalDate isoDate) {
        this.isoDate = isoDate;
    }

    //-----------------------------------------------------------------------
    @Override
    public Chrono getChronology() {
        return ISOChrono.INSTANCE;
    }

    //-----------------------------------------------------------------------
    @Override
    public long get(DateTimeField field) {
        if (field instanceof LocalDateTimeField) {
            switch ((LocalDateTimeField) field) {

                case DAY_OF_WEEK: return isoDate.getDayOfWeek().getValue();
                case DAY_OF_MONTH: return isoDate.getDayOfMonth();
                case DAY_OF_YEAR: return isoDate.getDayOfYear();
                case MONTH_OF_YEAR: return isoDate.getMonthOfYear().getValue();
                case YEAR_OF_ERA: return (isoDate.getYear() >= 1 ? isoDate.getYear() : 1 - isoDate.getYear());
                case YEAR: return isoDate.getYear();
                case ERA: return (isoDate.getYear() >= 1 ? 1 : 0);
            }
            throw new CalendricalException(field.getName() + " not valid for LocalDate");
        }
        return field.get(this);
    }

    @Override
    public ISODate with(DateTimeField field, long newValue) {
        if (field instanceof LocalDateTimeField) {
            LocalDateTimeField f = (LocalDateTimeField) field;
            f.checkValidValue(newValue);
            int nvalue = (int)newValue;

            switch (f) {
                case DAY_OF_WEEK: return plusDays(nvalue - getDayOfWeek().getValue());
                case DAY_OF_MONTH: return with(isoDate.withDayOfMonth(nvalue));
                case DAY_OF_YEAR: return with(isoDate.withDayOfYear(nvalue));
                case MONTH_OF_YEAR: return with(isoDate.withMonthOfYear(nvalue));
                case YEAR_OF_ERA: return with(isoDate.withYear(isoDate.getYear() >= 1 ? nvalue : (1 - nvalue)));
                case YEAR: return with(isoDate.withYear(nvalue));
                case ERA: return with(isoDate.withYear(1 - isoDate.getYear()));
            }
            throw new CalendricalException(field.getName() + " not valid for LocalDate");
        }
        return field.set(this, newValue);
    }

    //-----------------------------------------------------------------------
    @Override
    public ISODate plusYears(long years) {
        return with(isoDate.plusYears(years));
    }

    @Override
    public ISODate plusMonths(long months) {
        return with(isoDate.plusMonths(months));
    }

    @Override
    public ISODate plusDays(long days) {
        return with(isoDate.plusDays(days));
    }

    private ISODate with(LocalDate newDate) {
        return (newDate == isoDate ? this : new ISODate(newDate));
    }

    //-----------------------------------------------------------------------
    @Override
    public LocalDate toLocalDate() {
        return isoDate;
    }

}
