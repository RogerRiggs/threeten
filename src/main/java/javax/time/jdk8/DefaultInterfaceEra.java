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
package javax.time.jdk8;

import static javax.time.calendrical.ChronoField.ERA;

import java.util.Locale;

import javax.time.calendrical.ChronoField;
import javax.time.calendrical.DateTime;
import javax.time.calendrical.DateTimeField;
import javax.time.chrono.Chrono;
import javax.time.chrono.ChronoLocalDate;
import javax.time.chrono.Era;
import javax.time.format.DateTimeFormatterBuilder;
import javax.time.format.TextStyle;

/**
 * A temporary class providing implementations that will become default interface
 * methods once integrated into JDK 8.
 *
 * @param <C> the chronology of this era
 */
public abstract class DefaultInterfaceEra<C extends Chrono<C>>
        extends DefaultInterfaceDateTimeAccessor
        implements Era<C> {

    @Override
    public ChronoLocalDate<C> date(int year, int month, int day) {
        return getChrono().date(this, year, month, day);
    }

    @Override
    public ChronoLocalDate<C> dateFromYearDay(int year, int dayOfYear) {
        return getChrono().dateFromYearDay(this, year, dayOfYear);
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean isSupported(DateTimeField field) {
        if (field instanceof ChronoField) {
            return field == ERA;
        }
        return field != null && field.doIsSupported(this);
    }

    @Override
    public int get(DateTimeField field) {
        if (field == ERA) {
            return getValue();
        }
        return range(field).checkValidIntValue(getLong(field), field);
    }

    @Override
    public long getLong(DateTimeField field) {
        if (field == ERA) {
            return getValue();
        } else if (field instanceof ChronoField) {
            throw new IllegalArgumentException("Unsupported field: " + field.getName());
        }
        return field.doGet(this);
    }

    @Override
    public Era<C> with(DateTimeField field, long newValue) {
        if (field == ERA) {
            int eravalue = ((ChronoField) field).checkValidIntValue(newValue);
            return getChrono().eraOf(eravalue);
        } else if (field instanceof ChronoField) {
            throw new IllegalArgumentException("Unsupported field: " + field.getName());
        }
        return field.doSet(this, newValue);
    }

    //-------------------------------------------------------------------------
    @Override
    public DateTime doWithAdjustment(DateTime dateTime) {
        return dateTime.with(ERA, getValue());
    }

    @Override
    public <R> R query(Query<R> query) {
        if (query == Query.ZONE_ID) {
            return null;
        } else if (query == Query.CHRONO) {
            return (R) getChrono();
        }
        return query.doQuery(this);
    }

    //-----------------------------------------------------------------------
    @Override
    public String getText(TextStyle style, Locale locale) {
        return new DateTimeFormatterBuilder().appendText(ERA, style).toFormatter(locale).print(this);
    }

}
