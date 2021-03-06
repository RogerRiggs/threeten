/*
 * Copyright (c) 2007-2012, Stephen Colebourne & Michael Nascimento Santos
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
package javax.time;

import static javax.time.DateTimeConstants.SECONDS_PER_HOUR;
import static javax.time.DateTimeConstants.SECONDS_PER_MINUTE;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import javax.time.calendrical.ChronoField;
import javax.time.calendrical.ChronoUnit;
import javax.time.calendrical.DateTime;
import javax.time.calendrical.DateTime.WithAdjuster;
import javax.time.calendrical.DateTimeAccessor;
import javax.time.calendrical.DateTimeAdjusters;
import javax.time.calendrical.DateTimeField;
import javax.time.calendrical.DateTimeValueRange;
import javax.time.calendrical.PeriodUnit;
import javax.time.chrono.ChronoZonedDateTime;
import javax.time.chrono.ISOChrono;
import javax.time.format.CalendricalFormatter;
import javax.time.format.DateTimeFormatters;
import javax.time.format.DateTimeParseException;
import javax.time.jdk8.DefaultInterfaceChronoZonedDateTime;
import javax.time.zone.ZoneOffsetTransition;
import javax.time.zone.ZoneResolver;
import javax.time.zone.ZoneResolvers;
import javax.time.zone.ZoneRules;

/**
 * A date-time with a time-zone in the ISO-8601 calendar system,
 * such as {@code 2007-12-03T10:15:30+01:00 Europe/Paris}.
 * <p>
 * {@code ZonedDateTime} is an immutable representation of a date-time with a time-zone.
 * This class stores all date and time fields, to a precision of nanoseconds,
 * as well as a time-zone and zone offset. For example, the value
 * "2nd October 2007 at 13:45.30.123456789 +02:00 in the Europe/Paris time-zone"
 * can be stored in a {@code ZonedDateTime}.
 * <p>
 * The purpose of storing the time-zone is to distinguish the ambiguous case where
 * the local time-line overlaps, typically as a result of the end of daylight time.
 * Information about the local-time can be obtained using methods on the time-zone.
 * <p>
 * This class provides control over what happens at these cutover points
 * (typically a gap in spring and an overlap in autumn). The {@link ZoneResolver}
 * interface and implementations in {@link ZoneResolvers} provide strategies for
 * handling these cases. The methods {@link #withEarlierOffsetAtOverlap()} and
 * {@link #withLaterOffsetAtOverlap()} provide further control for overlaps.
 *
 * <h4>Implementation notes</h4>
 * This class is immutable and thread-safe.
 */
public final class ZonedDateTime
        extends DefaultInterfaceChronoZonedDateTime<ISOChrono>
        implements ChronoZonedDateTime<ISOChrono>, DateTime, WithAdjuster, Serializable {

    /**
     * Serialization version.
     */
    private static final long serialVersionUID = -6260982410461394882L;

    /**
     * The offset date-time.
     */
    private final OffsetDateTime dateTime;
    /**
     * The time-zone.
     */
    private final ZoneId zone;

    //-----------------------------------------------------------------------
    /**
     * Obtains the current date-time from the system clock in the default time-zone.
     * <p>
     * This will query the {@link Clock#systemDefaultZone() system clock} in the default
     * time-zone to obtain the current date-time.
     * The zone and offset will be set based on the time-zone in the clock.
     * <p>
     * Using this method will prevent the ability to use an alternate clock for testing
     * because the clock is hard-coded.
     *
     * @return the current date-time using the system clock, not null
     */
    public static ZonedDateTime now() {
        return now(Clock.systemDefaultZone());
    }

    /**
     * Obtains the current date-time from the specified clock.
     * <p>
     * This will query the specified clock to obtain the current time.
     * The zone and offset will be set based on the time-zone in the clock.
     * <p>
     * Using this method allows the use of an alternate clock for testing.
     * The alternate clock may be introduced using {@link Clock dependency injection}.
     *
     * @param clock  the clock to use, not null
     * @return the current date-time, not null
     */
    public static ZonedDateTime now(Clock clock) {
        Objects.requireNonNull(clock, "clock");
        final Instant now = clock.instant();  // called once
        return ofInstant(now, clock.getZone());
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code ZonedDateTime} from year, month,
     * day, hour, minute, second, nanosecond and time-zone
     * where the date-time must be valid for the time-zone.
     * <p>
     * The day must be valid for the year and month, otherwise an exception will be thrown.
     * <p>
     * The local date-time must be valid for the time-zone.
     * If the time is invalid for the zone, due to either being a gap or an overlap,
     * then an exception will be thrown.
     *
     * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
     * @param month  the month-of-year to represent, not null
     * @param dayOfMonth  the day-of-month to represent, from 1 to 31
     * @param hour  the hour-of-day to represent, from 0 to 23
     * @param minute  the minute-of-hour to represent, from 0 to 59
     * @param second  the second-of-minute to represent, from 0 to 59
     * @param nanoOfSecond  the nano-of-second to represent, from 0 to 999,999,999
     * @param zone  the time-zone, not null
     * @return the zoned date-time, not null
     * @throws DateTimeException if the value of any field is out of range
     * @throws DateTimeException if the day-of-month is invalid for the month-year
     * @throws DateTimeException if the local date-time is invalid for the time-zone
     */
    public static ZonedDateTime of(int year, Month month, int dayOfMonth,
            int hour, int minute, int second, int nanoOfSecond, ZoneId zone) {
        return of(year, month, dayOfMonth, hour, minute, second, nanoOfSecond, zone, ZoneResolvers.strict());
    }

    /**
     * Obtains an instance of {@code ZonedDateTime} from year, month,
     * day, hour, minute, second, nanosecond and time-zone
     * providing a resolver to handle an invalid date-time.
     * <p>
     * The day must be valid for the year and month, otherwise an exception will be thrown.
     * <p>
     * The local date-time must be valid for the time-zone.
     * If the time is invalid for the zone, due to either being a gap or an overlap,
     * then the resolver will determine what action to take.
     * See {@link ZoneResolvers} for common resolver implementations.
     *
     * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
     * @param month  the month-of-year to represent, not null
     * @param dayOfMonth  the day-of-month to represent, from 1 to 31
     * @param hour  the hour-of-day to represent, from 0 to 23
     * @param minute  the minute-of-hour to represent, from 0 to 59
     * @param second  the second-of-minute to represent, from 0 to 59
     * @param nanoOfSecond  the nano-of-second to represent, from 0 to 999,999,999
     * @param zone  the time-zone, not null
     * @param resolver  the resolver from local date-time to zoned, not null
     * @return the zoned date-time, not null
     * @throws DateTimeException if the value of any field is out of range
     * @throws DateTimeException if the day-of-month is invalid for the month-year
     * @throws DateTimeException if the resolver cannot resolve an invalid local date-time
     */
    public static ZonedDateTime of(int year, Month month, int dayOfMonth,
            int hour, int minute, int second, int nanoOfSecond,
            ZoneId zone, ZoneResolver resolver) {
        LocalDateTime dt = LocalDateTime.of(year, month, dayOfMonth, hour, minute, second, nanoOfSecond);
        return resolve(dt, zone, null, resolver);
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code ZonedDateTime} from year, month,
     * day, hour, minute, second, nanosecond and time-zone
     * where the date-time must be valid for the time-zone.
     * <p>
     * The day must be valid for the year and month, otherwise an exception will be thrown.
     * <p>
     * The local date-time must be valid for the time-zone.
     * If the time is invalid for the zone, due to either being a gap or an overlap,
     * then an exception will be thrown.
     *
     * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
     * @param month  the month-of-year to represent, from 1 (January) to 12 (December)
     * @param dayOfMonth  the day-of-month to represent, from 1 to 31
     * @param hour  the hour-of-day to represent, from 0 to 23
     * @param minute  the minute-of-hour to represent, from 0 to 59
     * @param second  the second-of-minute to represent, from 0 to 59
     * @param nanoOfSecond  the nano-of-second to represent, from 0 to 999,999,999
     * @param zone  the time-zone, not null
     * @return the zoned date-time, not null
     * @throws DateTimeException if the value of any field is out of range
     * @throws DateTimeException if the day-of-month is invalid for the month-year
     * @throws DateTimeException if the local date-time is invalid for the time-zone
     */
    public static ZonedDateTime of(int year, int month, int dayOfMonth,
            int hour, int minute, int second, int nanoOfSecond, ZoneId zone) {
        return of(year, month, dayOfMonth,
                hour, minute, second, nanoOfSecond, zone, ZoneResolvers.strict());
    }

    /**
     * Obtains an instance of {@code ZonedDateTime} from year, month,
     * day, hour, minute, second, nanosecond and time-zone
     * providing a resolver to handle an invalid date-time.
     * <p>
     * The day must be valid for the year and month, otherwise an exception will be thrown.
     * <p>
     * The local date-time must be valid for the time-zone.
     * If the time is invalid for the zone, due to either being a gap or an overlap,
     * then the resolver will determine what action to take.
     * See {@link ZoneResolvers} for common resolver implementations.
     *
     * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
     * @param month  the month-of-year to represent, from 1 (January) to 12 (December)
     * @param dayOfMonth  the day-of-month to represent, from 1 to 31
     * @param hour  the hour-of-day to represent, from 0 to 23
     * @param minute  the minute-of-hour to represent, from 0 to 59
     * @param second  the second-of-minute to represent, from 0 to 59
     * @param nanoOfSecond  the nano-of-second to represent, from 0 to 999,999,999
     * @param zone  the time-zone, not null
     * @param resolver  the resolver from local date-time to zoned, not null
     * @return the zoned date-time, not null
     * @throws DateTimeException if the value of any field is out of range
     * @throws DateTimeException if the day-of-month is invalid for the month-year
     * @throws DateTimeException if the resolver cannot resolve an invalid local date-time
     */
    public static ZonedDateTime of(int year, int month, int dayOfMonth,
            int hour, int minute, int second, int nanoOfSecond,
            ZoneId zone, ZoneResolver resolver) {
        LocalDateTime dt = LocalDateTime.of(year, month, dayOfMonth,
                                    hour, minute, second, nanoOfSecond);
        return resolve(dt, zone, null, resolver);
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code ZonedDateTime} from a local date and time
     * where the date-time must be valid for the time-zone.
     * <p>
     * This factory creates a {@code ZonedDateTime} from a date, time and time-zone.
     * If the time is invalid for the zone, due to either being a gap or an overlap,
     * then an exception will be thrown.
     *
     * @param date  the local date, not null
     * @param time  the local time, not null
     * @param zone  the time-zone, not null
     * @return the zoned date-time, not null
     * @throws DateTimeException if the local date-time is invalid for the time-zone
     */
    public static ZonedDateTime of(LocalDate date, LocalTime time, ZoneId zone) {
        return of(date, time, zone, ZoneResolvers.strict());
    }

    /**
     * Obtains an instance of {@code ZonedDateTime} from a local date and time
     * providing a resolver to handle an invalid date-time.
     * <p>
     * This factory creates a {@code ZonedDateTime} from a date, time and time-zone.
     * If the time is invalid for the zone, due to either being a gap or an overlap,
     * then the resolver will determine what action to take.
     * See {@link ZoneResolvers} for common resolver implementations.
     *
     * @param date  the local date, not null
     * @param time  the local time, not null
     * @param zone  the time-zone, not null
     * @param resolver  the resolver from local date-time to zoned, not null
     * @return the zoned date-time, not null
     * @throws DateTimeException if the resolver cannot resolve an invalid local date-time
     */
    public static ZonedDateTime of(LocalDate date, LocalTime time, ZoneId zone, ZoneResolver resolver) {
        return resolve(LocalDateTime.of(date, time), zone, null, resolver);
    }

    /**
     * Obtains an instance of {@code ZonedDateTime} from a local date-time
     * where the date-time must be valid for the time-zone.
     * <p>
     * This factory creates a {@code ZonedDateTime} from a date-time and time-zone.
     * If the time is invalid for the zone, due to either being a gap or an overlap,
     * then an exception will be thrown.
     *
     * @param dateTime  the local date-time, not null
     * @param zone  the time-zone, not null
     * @return the zoned date-time, not null
     * @throws DateTimeException if the local date-time is invalid for the time-zone
     */
    public static ZonedDateTime of(LocalDateTime dateTime, ZoneId zone) {
        return of(dateTime, zone, ZoneResolvers.strict());
    }

    /**
     * Obtains an instance of {@code ZonedDateTime} from a local date-time
     * providing a resolver to handle an invalid date-time.
     * <p>
     * This factory creates a {@code ZonedDateTime} from a date-time and time-zone.
     * If the time is invalid for the zone, due to either being a gap or an overlap,
     * then the resolver will determine what action to take.
     * See {@link ZoneResolvers} for common resolver implementations.
     *
     * @param dateTime  the local date-time, not null
     * @param zone  the time-zone, not null
     * @param resolver  the resolver from local date-time to zoned, not null
     * @return the zoned date-time, not null
     * @throws DateTimeException if the resolver cannot resolve an invalid local date-time
     */
    public static ZonedDateTime of(LocalDateTime dateTime, ZoneId zone, ZoneResolver resolver) {
        return resolve(dateTime, zone, null, resolver);
    }

    /**
     * Obtains an instance of {@code ZonedDateTime} from an {@code OffsetDateTime}
     * ensuring that the offset provided is valid for the time-zone.
     * <p>
     * This factory creates a {@code ZonedDateTime} from an offset date-time and time-zone.
     * If the date-time is invalid for the zone due to a time-line gap then an exception is thrown.
     * Otherwise, the offset is checked against the zone to ensure it is valid.
     * <p>
     * An alternative to this method is {@link #ofInstant}. This method will retain
     * the date and time and throw an exception if the offset is invalid.
     * The {@code ofInstant} method will change the date and time if necessary
     * to retain the same instant.
     *
     * @param dateTime  the offset date-time to use, not null
     * @param zone  the time-zone, not null
     * @return the zoned date-time, not null
     * @throws DateTimeException if no rules can be found for the zone
     * @throws DateTimeException if the date-time is invalid due to a gap in the local time-line
     * @throws DateTimeException if the offset is invalid for the time-zone at the date-time
     */
    public static ZonedDateTime of(OffsetDateTime dateTime, ZoneId zone) {
        Objects.requireNonNull(dateTime, "dateTime");
        Objects.requireNonNull(zone, "zone");
        LocalDateTime inputLDT = dateTime.getDateTime();
        ZoneOffset inputOffset = dateTime.getOffset();
        ZoneRules rules = zone.getRules();  // latest rules version
        List<ZoneOffset> validOffsets = rules.getValidOffsets(inputLDT);
        if (validOffsets.contains(inputOffset) == false) {
            if (validOffsets.size() == 0) {
                throw new DateTimeException("The local time " + inputLDT +
                        " does not exist in time-zone " + zone + " due to a daylight savings gap");
            }
            throw new DateTimeException("The offset in the date-time " + dateTime +
                    " is invalid for time-zone " + zone);
        }
        return new ZonedDateTime(dateTime, zone);
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code ZonedDateTime} from an {@code Instant}
     * using the UTC zone.
     * <p>
     * The resulting date-time represents exactly the same instant on the time-line.
     * Calling {@link #toInstant()} will return an instant equal to the one used here.
     * <p>
     * If the instant represents a point on the time-line outside the supported year
     * range then an exception will be thrown.
     *
     * @param instant  the instant to create the date-time from, not null
     * @return the zoned date-time in UTC, not null
     * @throws DateTimeException if the result exceeds the supported range
     */
    public static ZonedDateTime ofInstantUTC(Instant instant) {
        return ofInstant(instant, ZoneId.UTC);
    }

    /**
     * Obtains an instance of {@code ZonedDateTime} from an {@code Instant}.
     * <p>
     * The resulting date-time represents exactly the same instant on the time-line.
     * Calling {@link #toInstant()} will return an instant equal to the one used here.
     * <p>
     * If the instant represents a point on the time-line outside the supported year
     * range then an exception will be thrown.
     *
     * @param instant  the instant to create the date-time from, not null
     * @param zone  the time-zone to use, not null
     * @return the zoned date-time, not null
     * @throws DateTimeException if the result exceeds the supported range
     */
    public static ZonedDateTime ofInstant(Instant instant, ZoneId zone) {
        Objects.requireNonNull(instant, "instant");
        Objects.requireNonNull(zone, "zone");
        ZoneRules rules = zone.getRules();
        OffsetDateTime offsetDT = OffsetDateTime.ofInstant(instant, rules.getOffset(instant));
        return new ZonedDateTime(offsetDT, zone);
    }

    /**
     * Obtains an instance of {@code ZonedDateTime} from an {@code OffsetDateTime}.
     * <p>
     * The resulting date-time represents exactly the same instant on the time-line.
     * As such, the resulting local date-time may be different from the input.
     * <p>
     * If the instant represents a point on the time-line outside the supported year
     * range then an exception will be thrown.
     *
     * @param instantDateTime  the instant to create the date-time from, not null
     * @param zone  the time-zone to use, not null
     * @return the zoned date-time, not null
     * @throws DateTimeException if the result exceeds the supported range
     */
    public static ZonedDateTime ofInstant(OffsetDateTime instantDateTime, ZoneId zone) {
        Objects.requireNonNull(instantDateTime, "instantDateTime");
        Objects.requireNonNull(zone, "zone");
        ZoneRules rules = zone.getRules();
        if (rules.isValidOffset(instantDateTime.getDateTime(), instantDateTime.getOffset()) == false) {  // avoids toInstant()
            instantDateTime = instantDateTime.withOffsetSameInstant(rules.getOffset(instantDateTime.toInstant()));
        }
        return new ZonedDateTime(instantDateTime, zone);
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code ZonedDateTime} using seconds from the
     * epoch of 1970-01-01T00:00:00Z.
     * <p>
     * The nanosecond field is set to zero.
     *
     * @param epochSecond  the number of seconds from the epoch of 1970-01-01T00:00:00Z
     * @param zone  the time-zone, not null
     * @return the zoned date-time, not null
     * @throws DateTimeException if the result exceeds the supported range
     */
    public static ZonedDateTime ofEpochSecond(long epochSecond, ZoneId zone) {
        Objects.requireNonNull(zone, "zone");
        return create(epochSecond, 0, zone);
    }

    /**
     * Obtains an instance of {@code ZonedDateTime} using seconds from the
     * epoch of 1970-01-01T00:00:00Z.
     *
     * @param epochSecond  the number of seconds from the epoch of 1970-01-01T00:00:00Z
     * @param nanoOfSecond  the nanosecond within the second, from 0 to 999,999,999
     * @param zone  the time-zone, not null
     * @return the zoned date-time, not null
     * @throws DateTimeException if the result exceeds the supported range
     */
    private static ZonedDateTime create(long epochSecond, int nanoOfSecond, ZoneId zone) {
        ZoneRules rules = zone.getRules();  // latest rules version
        Instant instant = Instant.ofEpochSecond(epochSecond, nanoOfSecond);  // TODO: rules should be queryable by epochSeconds
        OffsetDateTime offsetDT = OffsetDateTime.create(epochSecond, nanoOfSecond, rules.getOffset(instant));
        return new ZonedDateTime(offsetDT, zone);
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code ZonedDateTime} from a date-time object.
     * <p>
     * A {@code DateTimeAccessor} represents some form of date and time information.
     * This factory converts the arbitrary date-time object to an instance of {@code ZonedDateTime}.
     *
     * @param dateTime  the date-time object to convert, not null
     * @return the zoned date-time, not null
     * @throws DateTimeException if unable to convert to an {@code ZonedDateTime}
     */
    public static ZonedDateTime from(DateTimeAccessor dateTime) {
        if (dateTime instanceof ZonedDateTime) {
            return (ZonedDateTime) dateTime;
        }
        try {
            ZoneId zone = ZoneId.from(dateTime);
            try {
                OffsetDateTime odt = OffsetDateTime.from(dateTime);
                return ofInstant(odt, zone);

            } catch (DateTimeException ex1) {
                try {
                    Instant instant = Instant.from(dateTime);
                    return ofInstant(instant, zone);

                } catch (DateTimeException ex2) {
                    LocalDateTime ldt = LocalDateTime.from(dateTime);
                    return of(ldt, zone, ZoneResolvers.postGapPreOverlap());
                }
            }
        } catch (DateTimeException ex) {
            throw new DateTimeException("Unable to convert date-time to ZonedDateTime: " + dateTime.getClass(), ex);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code ZonedDateTime} from a text string such as
     * {@code 2007-12-03T10:15:30+01:00[Europe/Paris]}.
     * <p>
     * The string must represent a valid date-time and is parsed using
     * {@link javax.time.format.DateTimeFormatters#isoZonedDateTime()}.
     *
     * @param text  the text to parse such as "2007-12-03T10:15:30+01:00[Europe/Paris]", not null
     * @return the parsed zoned date-time, not null
     * @throws DateTimeParseException if the text cannot be parsed
     */
    public static ZonedDateTime parse(CharSequence text) {
        return parse(text, DateTimeFormatters.isoZonedDateTime());
    }

    /**
     * Obtains an instance of {@code ZonedDateTime} from a text string using a specific formatter.
     * <p>
     * The text is parsed using the formatter, returning a date-time.
     *
     * @param text  the text to parse, not null
     * @param formatter  the formatter to use, not null
     * @return the parsed zoned date-time, not null
     * @throws DateTimeParseException if the text cannot be parsed
     */
    public static ZonedDateTime parse(CharSequence text, CalendricalFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.parse(text, ZonedDateTime.class);
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code ZonedDateTime}.
     *
     * @param desiredLocalDateTime  the date-time, not null
     * @param zone  the time-zone, not null
     * @param oldDateTime  the old date-time prior to the calculation, may be null
     * @param resolver  the resolver from local date-time to zoned, not null
     * @return the zoned date-time, not null
     * @throws DateTimeException if the date-time cannot be resolved
     */
    static ZonedDateTime resolve(LocalDateTime desiredLocalDateTime, ZoneId zone, OffsetDateTime oldDateTime, ZoneResolver resolver) {
        Objects.requireNonNull(desiredLocalDateTime, "desiredLocalDateTime");
        Objects.requireNonNull(zone, "zone");
        Objects.requireNonNull(resolver, "resolver");
        ZoneRules rules = zone.getRules();
        List<ZoneOffset> validOffsets = rules.getValidOffsets(desiredLocalDateTime);
        OffsetDateTime offsetDT;
        if (validOffsets.size() == 1) {
            offsetDT = OffsetDateTime.of(desiredLocalDateTime, validOffsets.get(0));
        } else {
            ZoneOffsetTransition trans = rules.getTransition(desiredLocalDateTime);
            offsetDT = resolver.resolve(desiredLocalDateTime, trans, rules, zone, oldDateTime);
            if (((offsetDT.getDateTime() == desiredLocalDateTime && validOffsets.contains(offsetDT.getOffset())) ||
                    rules.isValidOffset(offsetDT.getDateTime(), offsetDT.getOffset())) == false) {
                throw new DateTimeException(
                    "ZoneResolver implementation must return a valid date-time and offset for the zone: " + resolver.getClass().getName());
            }
        }
        return new ZonedDateTime(offsetDT, zone);
    }

    //-----------------------------------------------------------------------
    /**
     * Constructor.
     *
     * @param dateTime  the date-time, validated as not null
     * @param zone  the time-zone, validated as not null
     */
    private ZonedDateTime(OffsetDateTime dateTime, ZoneId zone) {
        this.dateTime = dateTime;
        this.zone = zone;
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean isSupported(DateTimeField field) {
        return field instanceof ChronoField || (field != null && field.doIsSupported(this));
    }

    @Override
    public DateTimeValueRange range(DateTimeField field) {
        if (field instanceof ChronoField) {
            return dateTime.range(field);
        }
        return field.doRange(this);
    }

    @Override
    public int get(DateTimeField field) {
        if (field instanceof ChronoField) {
            switch ((ChronoField) field) {
                case INSTANT_SECONDS: throw new DateTimeException("Field too large for an int: " + field);
                case OFFSET_SECONDS: return getOffset().getTotalSeconds();
            }
            return dateTime.get(field);
        }
        return super.get(field);
    }

    @Override
    public long getLong(DateTimeField field) {
        if (field instanceof ChronoField) {
            switch ((ChronoField) field) {
                case INSTANT_SECONDS: return toEpochSecond();
                case OFFSET_SECONDS: return getOffset().getTotalSeconds();
            }
            return dateTime.getLong(field);
        }
        return field.doGet(this);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the zone offset, such as '+01:00'.
     * <p>
     * This is the offset of the local date-time from UTC/Greenwich.
     *
     * @return the zone offset, not null
     */
    @Override
    public ZoneOffset getOffset() {
        return dateTime.getOffset();
    }

    /**
     * Returns a copy of this ZonedDateTime changing the zone offset to the
     * earlier of the two valid offsets at a local time-line overlap.
     * <p>
     * This method only has any effect when the local time-line overlaps, such as
     * at an autumn daylight savings cutover. In this scenario, there are two
     * valid offsets for the local date-time. Calling this method will return
     * a zoned date-time with the earlier of the two selected.
     * <p>
     * If this method is called when it is not an overlap, {@code this}
     * is returned.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @return a {@code ZonedDateTime} based on this date-time with the earlier offset, not null
     * @throws DateTimeException if no rules can be found for the zone
     * @throws DateTimeException if no rules are valid for this date-time
     */
    @Override
    public ZonedDateTime withEarlierOffsetAtOverlap() {
        ZoneOffsetTransition trans = getZone().getRules().getTransition(getDateTime());
        if (trans != null) {
            ZoneOffset offset = trans.getOffsetBefore();
            if (offset.equals(getOffset()) == false) {
                OffsetDateTime newDT = dateTime.withOffsetSameLocal(offset);
                return new ZonedDateTime(newDT, zone);
            }
        }
        return this;
    }

    /**
     * Returns a copy of this ZonedDateTime changing the zone offset to the
     * later of the two valid offsets at a local time-line overlap.
     * <p>
     * This method only has any effect when the local time-line overlaps, such as
     * at an autumn daylight savings cutover. In this scenario, there are two
     * valid offsets for the local date-time. Calling this method will return
     * a zoned date-time with the later of the two selected.
     * <p>
     * If this method is called when it is not an overlap, {@code this}
     * is returned.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @return a {@code ZonedDateTime} based on this date-time with the later offset, not null
     * @throws DateTimeException if no rules can be found for the zone
     * @throws DateTimeException if no rules are valid for this date-time
     */
    @Override
    public ZonedDateTime withLaterOffsetAtOverlap() {
        ZoneOffsetTransition trans = getZone().getRules().getTransition(getDateTime());
        if (trans != null) {
            ZoneOffset offset = trans.getOffsetAfter();
            if (offset.equals(getOffset()) == false) {
                OffsetDateTime newDT = dateTime.withOffsetSameLocal(offset);
                return new ZonedDateTime(newDT, zone);
            }
        }
        return this;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the time-zone, such as 'Europe/Paris'.
     * <p>
     * This returns the stored time-zone ID used to determine the time-zone rules.
     *
     * @return the time-zone, not null
     */
    @Override
    public ZoneId getZone() {
        return zone;
    }

    /**
     * Returns a copy of this ZonedDateTime with a different time-zone,
     * retaining the local date-time if possible.
     * <p>
     * This method changes the time-zone and retains the local date-time.
     * The local date-time is only changed if it is invalid for the new zone.
     * In that case, the {@link ZoneResolvers#retainOffset() retain offset} resolver is used.
     * <p>
     * To change the zone and adjust the local date-time,
     * use {@link #withZoneSameInstant(ZoneId)}.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param zone  the time-zone to change to, not null
     * @return a {@code ZonedDateTime} based on this date-time with the requested zone, not null
     */
    @Override
    public ZonedDateTime withZoneSameLocal(ZoneId zone) {
        return withZoneSameLocal(zone, ZoneResolvers.retainOffset());
    }

    /**
     * Returns a copy of this ZonedDateTime with a different time-zone,
     * retaining the local date-time if possible.
     * <p>
     * This method changes the time-zone and retains the local date-time.
     * The local date-time is only changed if it is invalid for the new zone.
     * In that case, the specified resolver is used.
     * <p>
     * To change the zone and adjust the local date-time,
     * use {@link #withZoneSameInstant(ZoneId)}.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param zone  the time-zone to change to, not null
     * @param resolver  the resolver to use, not null
     * @return a {@code ZonedDateTime} based on this date-time with the requested zone, not null
     */
    @Override
    public ZonedDateTime withZoneSameLocal(ZoneId zone, ZoneResolver resolver) {
        Objects.requireNonNull(zone, "zone");
        Objects.requireNonNull(resolver, "resolver");
        return zone == this.zone ? this :
            resolve(dateTime.getDateTime(), zone, dateTime, resolver);
    }

    /**
     * Returns a copy of this ZonedDateTime with a different time-zone,
     * retaining the instant.
     * <p>
     * This method changes the time-zone and retains the instant.
     * This normally results in a change to the local date-time.
     * <p>
     * This method is based on retaining the same instant, thus gaps and overlaps
     * in the local time-line have no effect on the result.
     * <p>
     * To change the offset while keeping the local time,
     * use {@link #withZoneSameLocal(ZoneId)}.
     *
     * @param zone  the time-zone to change to, not null
     * @return a {@code ZonedDateTime} based on this date-time with the requested zone, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    @Override
    public ZonedDateTime withZoneSameInstant(ZoneId zone) {
        return zone == this.zone ? this : ofInstant(dateTime, zone);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the {@code LocalDateTime} part of this offset date-time.
     * <p>
     * This returns a {@code LocalDateTime} with the same year, month, day and time
     * as this date-time.
     *
     * @return the local date-time part of this date-time, not null
     */
    @Override  // override for return type
    public LocalDateTime getDateTime() {
        return dateTime.getDateTime();
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the {@code LocalDate} part of this date-time.
     * <p>
     * This returns a {@code LocalDate} with the same year, month and day
     * as this date-time.
     *
     * @return the date part of this date-time, not null
     */
    @Override  // override for return type
    public LocalDate getDate() {
        return dateTime.getDate();
    }

    /**
     * Gets the year field.
     * <p>
     * This method returns the primitive {@code int} value for the year.
     * <p>
     * The year returned by this method is proleptic as per {@code get(YEAR)}.
     * To obtain the year-of-era, use {@code get(YEAR_OF_ERA}.
     *
     * @return the year, from MIN_YEAR to MAX_YEAR
     */
    public int getYear() {
        return dateTime.getYear();
    }

    /**
     * Gets the month-of-year field from 1 to 12.
     * <p>
     * This method returns the month as an {@code int} from 1 to 12.
     * Application code is frequently clearer if the enum {@link Month}
     * is used by calling {@link #getMonth()}.
     *
     * @return the month-of-year, from 1 to 12
     * @see #getMonth()
     */
    public int getMonthValue() {
        return dateTime.getMonthValue();
    }

    /**
     * Gets the month-of-year field using the {@code Month} enum.
     * <p>
     * This method returns the enum {@link Month} for the month.
     * This avoids confusion as to what {@code int} values mean.
     * If you need access to the primitive {@code int} value then the enum
     * provides the {@link Month#getValue() int value}.
     *
     * @return the month-of-year, not null
     * @see #getMonthValue()
     */
    public Month getMonth() {
        return dateTime.getMonth();
    }

    /**
     * Gets the day-of-month field.
     * <p>
     * This method returns the primitive {@code int} value for the day-of-month.
     *
     * @return the day-of-month, from 1 to 31
     */
    public int getDayOfMonth() {
        return dateTime.getDayOfMonth();
    }

    /**
     * Gets the day-of-year field.
     * <p>
     * This method returns the primitive {@code int} value for the day-of-year.
     *
     * @return the day-of-year, from 1 to 365, or 366 in a leap year
     */
    public int getDayOfYear() {
        return dateTime.getDayOfYear();
    }

    /**
     * Gets the day-of-week field, which is an enum {@code DayOfWeek}.
     * <p>
     * This method returns the enum {@link DayOfWeek} for the day-of-week.
     * This avoids confusion as to what {@code int} values mean.
     * If you need access to the primitive {@code int} value then the enum
     * provides the {@link DayOfWeek#getValue() int value}.
     * <p>
     * Additional information can be obtained from the {@code DayOfWeek}.
     * This includes textual names of the values.
     *
     * @return the day-of-week, not null
     */
    public DayOfWeek getDayOfWeek() {
        return dateTime.getDayOfWeek();
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the {@code LocalTime} part of this date-time.
     * <p>
     * This returns a {@code LocalTime} with the same hour, minute, second and
     * nanosecond as this date-time.
     *
     * @return the time part of this date-time, not null
     */
    @Override  // override for Javadoc and performance
    public LocalTime getTime() {
        return dateTime.getTime();
    }

    /**
     * Gets the hour-of-day field.
     *
     * @return the hour-of-day, from 0 to 23
     */
    public int getHour() {
        return dateTime.getHour();
    }

    /**
     * Gets the minute-of-hour field.
     *
     * @return the minute-of-hour, from 0 to 59
     */
    public int getMinute() {
        return dateTime.getMinute();
    }

    /**
     * Gets the second-of-minute field.
     *
     * @return the second-of-minute, from 0 to 59
     */
    public int getSecond() {
        return dateTime.getSecond();
    }

    /**
     * Gets the nano-of-second field.
     *
     * @return the nano-of-second, from 0 to 999,999,999
     */
    public int getNano() {
        return dateTime.getNano();
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this {@code ZonedDateTime} with the local date-time altered.
     * <p>
     * This method returns an object with the same {@code ZoneId} and the
     * specified {@code LocalDateTime}.
     * <p>
     * If the adjusted date results in a date-time that is invalid, then the
     * {@link ZoneResolvers#retainOffset()} resolver is used.
     *
     * @param dateTime  the local date-time to change to, not null
     * @return a {@code ZonedDateTime} based on this time with the requested date-time, not null
     */
    public ZonedDateTime withDateTime(LocalDateTime dateTime) {
        return withDateTime(dateTime, ZoneResolvers.retainOffset());
    }

    /**
     * Returns a copy of this {@code ZonedDateTime} with the local date-time altered,
     * providing a resolver for invalid date-times.
     * <p>
     * This method returns an object with the same {@code ZoneId} and the
     * specified {@code LocalDateTime}.
     * <p>
     * If the adjusted date results in a date-time that is invalid, then the
     * specified resolver is used.
     *
     * @param dateTime  the local date-time to change to, not null
     * @param resolver  the resolver to use, not null
     * @return a {@code ZonedDateTime} based on this time with the requested date-time, not null
     */
    public ZonedDateTime withDateTime(LocalDateTime dateTime, ZoneResolver resolver) {
        Objects.requireNonNull(dateTime, "dateTime");
        Objects.requireNonNull(resolver, "resolver");
        return this.getDateTime().equals(dateTime) ?
                this : ZonedDateTime.resolve(dateTime, zone, this.dateTime, resolver);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns an adjusted date-time based on this date-time.
     * <p>
     * This adjusts the date-time according to the rules of the specified adjuster.
     * A simple adjuster might simply set the one of the fields, such as the year field.
     * A more complex adjuster might set the date-time to the last day of the month.
     * A selection of common adjustments is provided in {@link DateTimeAdjusters}.
     * These include finding the "last day of the month" and "next Wednesday".
     * The adjuster is responsible for handling special cases, such as the varying
     * lengths of month and leap years.
     * <p>
     * In addition, all principal classes implement the {@link WithAdjuster} interface,
     * including this one. For example, {@link LocalDate} implements the adjuster interface.
     * As such, this code will compile and run:
     * <pre>
     *  dateTime.with(date);
     * </pre>
     * <p>
     * If the adjusted date results in a date-time that is invalid, then the
     * {@link ZoneResolvers#retainOffset()} resolver is used.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param adjuster the adjuster to use, not null
     * @return a {@code ZonedDateTime} based on this date-time with the adjustment made, not null
     * @throws DateTimeException if the adjustment cannot be made
     */
    @Override
    public ZonedDateTime with(WithAdjuster adjuster) {
        return with(adjuster, ZoneResolvers.retainOffset());
    }

    /**
     * Returns an adjusted date-time based on this date-time
     * providing a resolver for invalid date-times.
     * <p>
     * This adjusts the date-time according to the rules of the specified adjuster.
     * A simple adjuster might simply set the one of the fields, such as the year field.
     * A more complex adjuster might set the date-time to the last day of the month.
     * A selection of common adjustments is provided in {@link DateTimeAdjusters}.
     * These include finding the "last day of the month" and "next Wednesday".
     * The adjuster is responsible for handling special cases, such as the varying
     * lengths of month and leap years.
     * <p>
     * In addition, all principal classes implement the {@link WithAdjuster} interface,
     * including this one. For example, {@link LocalDate} implements the adjuster interface.
     * As such, this code will compile and run:
     * <pre>
     *  dateTime.with(date);
     * </pre>
     * <p>
     * If the adjusted date results in a date-time that is invalid, then the
     * specified resolver is used.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param adjuster the adjuster to use, not null
     * @param resolver  the resolver to use, not null
     * @return a {@code ZonedDateTime} based on this date-time with the adjustment made, not null
     * @throws DateTimeException if the adjustment cannot be made
     */
    @Override
    public ZonedDateTime with(WithAdjuster adjuster, ZoneResolver resolver) {
        Objects.requireNonNull(adjuster, "adjuster");
        Objects.requireNonNull(resolver, "resolver");
        OffsetDateTime newDT = dateTime.with(adjuster);  // TODO: should adjust ZDT, not ODT
        return (newDT == dateTime ? this : resolve(newDT.getDateTime(), zone, dateTime, resolver));
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this date-time with the specified field altered.
     * <p>
     * This method returns a new date-time based on this date-time with a new value for the specified field.
     * This can be used to change any field, for example to set the year, month of day-of-month.
     * The offset is not part of the calculation and will be unchanged in the result.
     * <p>
     * In some cases, changing the specified field can cause the resulting date-time to become invalid,
     * such as changing the month from January to February would make the day-of-month 31 invalid.
     * In cases like this, the field is responsible for resolving the date. Typically it will choose
     * the previous valid date, which would be the last valid day of February in this example.
     * <p>
     * If the adjustment results in a date-time that is invalid for the zone,
     * then the {@link ZoneResolvers#retainOffset()} resolver is used.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param field  the field to set in the returned date-time, not null
     * @param newValue  the new value of the field in the returned date-time, not null
     * @return a {@code ZonedDateTime} based on this date-time with the specified field set, not null
     * @throws DateTimeException if the value is invalid
     */
    @Override
    public ZonedDateTime with(DateTimeField field, long newValue) {
        if (field instanceof ChronoField) {
            ChronoField f = (ChronoField) field;
            switch (f) {
                case INSTANT_SECONDS: return create(newValue, getNano(), zone);
                case OFFSET_SECONDS: {
                    ZoneOffset offset = ZoneOffset.ofTotalSeconds(f.checkValidIntValue(newValue));
                    OffsetDateTime odt = dateTime.withOffsetSameLocal(offset);
                    return ofInstant(odt, zone);
                }
            }
            return withDateTime(getDateTime().with(field, newValue));
        }
        return field.doWith(this, newValue);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this {@code ZonedDateTime} with the year value altered.
     * <p>
     * If the day-of-month is invalid for the year, it will be changed to the last valid day of the month.
     * If the adjustment results in a date-time that is invalid for the zone,
     * then the {@link ZoneResolvers#retainOffset()} resolver is used.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
     * @return a {@code ZonedDateTime} based on this date-time with the requested year, not null
     * @throws DateTimeException if the year value is invalid
     */
    public ZonedDateTime withYear(int year) {
        LocalDateTime newDT = dateTime.getDateTime().withYear(year);
        return (newDT == dateTime.getDateTime() ? this :
            resolve(newDT, zone, dateTime, ZoneResolvers.retainOffset()));
    }

    /**
     * Returns a copy of this {@code ZonedDateTime} with the month-of-year value altered.
     * <p>
     * If the day-of-month is invalid for the year, it will be changed to the last valid day of the month.
     * If the adjustment results in a date-time that is invalid for the zone,
     * then the {@link ZoneResolvers#retainOffset()} resolver is used.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param month  the month-of-year to represent, from 1 (January) to 12 (December)
     * @return a {@code ZonedDateTime} based on this date-time with the requested month, not null
     * @throws DateTimeException if the month value is invalid
     */
    public ZonedDateTime withMonth(int month) {
        LocalDateTime newDT = dateTime.getDateTime().withMonth(month);
        return (newDT == dateTime.getDateTime() ? this :
            resolve(newDT, zone, dateTime, ZoneResolvers.retainOffset()));
    }

    /**
     * Returns a copy of this {@code ZonedDateTime} with the day-of-month value altered.
     * <p>
     * If the adjustment results in a date-time that is invalid, then the
     * {@link ZoneResolvers#retainOffset()} resolver is used.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param dayOfMonth  the day-of-month to represent, from 1 to 31
     * @return a {@code ZonedDateTime} based on this date-time with the requested day, not null
     * @throws DateTimeException if the day-of-month value is invalid
     * @throws DateTimeException if the day-of-month is invalid for the month-year
     */
    public ZonedDateTime withDayOfMonth(int dayOfMonth) {
        LocalDateTime newDT = dateTime.getDateTime().withDayOfMonth(dayOfMonth);
        return (newDT == dateTime.getDateTime() ? this :
            resolve(newDT, zone, dateTime, ZoneResolvers.retainOffset()));
    }

    /**
     * Returns a copy of this {@code ZonedDateTime} with the day-of-year altered.
     * <p>
     * If the adjustment results in a date-time that is invalid, then the
     * {@link ZoneResolvers#retainOffset()} resolver is used.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param dayOfYear  the day-of-year to set in the returned date, from 1 to 365-366
     * @return a {@code ZonedDateTime} based on this date with the requested day, not null
     * @throws DateTimeException if the day-of-year value is invalid
     * @throws DateTimeException if the day-of-year is invalid for the year
     */
    public ZonedDateTime withDayOfYear(int dayOfYear) {
        LocalDateTime newDT = dateTime.getDateTime().withDayOfYear(dayOfYear);
        return (newDT == dateTime.getDateTime() ? this :
            resolve(newDT, zone, dateTime, ZoneResolvers.retainOffset()));
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this {@code ZonedDateTime} with the date values altered.
     * <p>
     * If the adjustment results in a date-time that is invalid, then the
     * {@link ZoneResolvers#retainOffset()} resolver is used.
     * <p>
     * This method will return a new instance with the same time fields,
     * but altered date fields.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
     * @param month  the month-of-year to represent, from 1 (January) to 12 (December)
     * @param dayOfMonth  the day-of-month to represent, from 1 to 31
     * @return a {@code ZonedDateTime} based on this date-time with the requested date, not null
     * @throws DateTimeException if the any field value is invalid
     * @throws DateTimeException if the day-of-month is invalid for the month-year
     */
    public ZonedDateTime withDate(int year, int month, int dayOfMonth) {
        LocalDateTime newDT = dateTime.getDateTime().withDate(year, month, dayOfMonth);
        return (newDT == dateTime.getDateTime() ? this :
            resolve(newDT, zone, dateTime, ZoneResolvers.retainOffset()));
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this {@code ZonedDateTime} with the hour-of-day value altered.
     * <p>
     * If the adjustment results in a date-time that is invalid, then the
     * {@link ZoneResolvers#retainOffset()} resolver is used.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param hour  the hour-of-day to represent, from 0 to 23
     * @return a {@code ZonedDateTime} based on this date-time with the requested hour, not null
     * @throws DateTimeException if the hour value is invalid
     */
    public ZonedDateTime withHour(int hour) {
        LocalDateTime newDT = dateTime.getDateTime().withHour(hour);
        return (newDT == dateTime.getDateTime() ? this :
            resolve(newDT, zone, dateTime, ZoneResolvers.retainOffset()));
    }

    /**
     * Returns a copy of this {@code ZonedDateTime} with the minute-of-hour value altered.
     * <p>
     * If the adjustment results in a date-time that is invalid, then the
     * {@link ZoneResolvers#retainOffset()} resolver is used.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param minute  the minute-of-hour to represent, from 0 to 59
     * @return a {@code ZonedDateTime} based on this date-time with the requested minute, not null
     * @throws DateTimeException if the minute value is invalid
     */
    public ZonedDateTime withMinute(int minute) {
        LocalDateTime newDT = dateTime.getDateTime().withMinute(minute);
        return (newDT == dateTime.getDateTime() ? this :
            resolve(newDT, zone, dateTime, ZoneResolvers.retainOffset()));
    }

    /**
     * Returns a copy of this {@code ZonedDateTime} with the second-of-minute value altered.
     * <p>
     * If the adjustment results in a date-time that is invalid, then the
     * {@link ZoneResolvers#retainOffset()} resolver is used.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param second  the second-of-minute to represent, from 0 to 59
     * @return a {@code ZonedDateTime} based on this date-time with the requested second, not null
     * @throws DateTimeException if the second value is invalid
     */
    public ZonedDateTime withSecond(int second) {
        LocalDateTime newDT = dateTime.getDateTime().withSecond(second);
        return (newDT == dateTime.getDateTime() ? this :
            resolve(newDT, zone, dateTime, ZoneResolvers.retainOffset()));
    }

    /**
     * Returns a copy of this {@code ZonedDateTime} with the nano-of-second value altered.
     * <p>
     * If the adjustment results in a date-time that is invalid, then the
     * {@link ZoneResolvers#retainOffset()} resolver is used.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param nanoOfSecond  the nano-of-second to represent, from 0 to 999,999,999
     * @return a {@code ZonedDateTime} based on this date-time with the requested nanosecond, not null
     * @throws DateTimeException if the nanos value is invalid
     */
    public ZonedDateTime withNano(int nanoOfSecond) {
        LocalDateTime newDT = dateTime.getDateTime().withNano(nanoOfSecond);
        return (newDT == dateTime.getDateTime() ? this :
            resolve(newDT, zone, dateTime, ZoneResolvers.retainOffset()));
    }

    /**
     * Returns a copy of this {@code ZonedDateTime} with the time values altered.
     * <p>
     * This method will return a new instance with the same date fields,
     * but altered time fields.
     * This is a shorthand for {@link #withTime(int,int,int)} and sets
     * the second field to zero.
     * <p>
     * If the adjustment results in a date-time that is invalid, then the
     * {@link ZoneResolvers#retainOffset()} resolver is used.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param hour  the hour-of-day to represent, from 0 to 23
     * @param minute  the minute-of-hour to represent, from 0 to 59
     * @return a {@code ZonedDateTime} based on this date-time with the requested time, not null
     * @throws DateTimeException if any field value is invalid
     */
    public ZonedDateTime withTime(int hour, int minute) {
        LocalDateTime newDT = dateTime.getDateTime().withTime(hour, minute);
        return (newDT == dateTime.getDateTime() ? this :
            resolve(newDT, zone, dateTime, ZoneResolvers.retainOffset()));
    }

    /**
     * Returns a copy of this {@code ZonedDateTime} with the time values altered.
     * <p>
     * If the adjustment results in a date-time that is invalid, then the
     * {@link ZoneResolvers#retainOffset()} resolver is used.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param hour  the hour-of-day to represent, from 0 to 23
     * @param minute  the minute-of-hour to represent, from 0 to 59
     * @param second  the second-of-minute to represent, from 0 to 59
     * @return a {@code ZonedDateTime} based on this date-time with the requested time, not null
     * @throws DateTimeException if any field value is invalid
     */
    public ZonedDateTime withTime(int hour, int minute, int second) {
        LocalDateTime newDT = dateTime.getDateTime().withTime(hour, minute, second);
        return (newDT == dateTime.getDateTime() ? this :
            resolve(newDT, zone, dateTime, ZoneResolvers.retainOffset()));
    }

    /**
     * Returns a copy of this {@code ZonedDateTime} with the time values altered.
     * <p>
     * If the adjustment results in a date-time that is invalid, then the
     * {@link ZoneResolvers#retainOffset()} resolver is used.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param hour  the hour-of-day to represent, from 0 to 23
     * @param minute  the minute-of-hour to represent, from 0 to 59
     * @param second  the second-of-minute to represent, from 0 to 59
     * @param nanoOfSecond  the nano-of-second to represent, from 0 to 999,999,999
     * @return a {@code ZonedDateTime} based on this date-time with the requested time, not null
     * @throws DateTimeException if any field value is invalid
     */
    public ZonedDateTime withTime(int hour, int minute, int second, int nanoOfSecond) {
        LocalDateTime newDT = dateTime.getDateTime().withTime(hour, minute, second, nanoOfSecond);
        return (newDT == dateTime.getDateTime() ? this :
            resolve(newDT, zone, dateTime, ZoneResolvers.retainOffset()));
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this date-time with the specified period added.
     * <p>
     * This method returns a new date-time based on this time with the specified period added.
     * The adjuster is typically {@link Period} but may be any other type implementing
     * the {@link javax.time.calendrical.DateTime.PlusAdjuster} interface.
     * The calculation is delegated to the specified adjuster, which typically calls
     * back to {@link #plus(long, PeriodUnit)}.
     * <p>
     * If the adjustment results in a date-time that is invalid for the zone,
     * then the {@link ZoneResolvers#retainOffset()} resolver is used.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param adjuster  the adjuster to use, not null
     * @return a {@code ZonedDateTime} based on this date-time with the addition made, not null
     * @throws DateTimeException if the addition cannot be made
     * @throws ArithmeticException if numeric overflow occurs
     */
    @Override
    public ZonedDateTime plus(PlusAdjuster adjuster) {
        return (ZonedDateTime) adjuster.doPlusAdjustment(this);
    }

    /**
     * Returns a copy of this date-time with the specified period added.
     * <p>
     * This method returns a new date-time based on this date-time with the specified period added.
     * This can be used to add any period that is defined by a unit, for example to add years, months or days.
     * The unit is responsible for the details of the calculation, including the resolution
     * of any edge cases in the calculation.
     * <p>
     * If the adjustment results in a date-time that is invalid for the zone,
     * then the {@link ZoneResolvers#retainOffset()} resolver is used.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param amountToAdd  the amount of the unit to add to the returned date-time, not null
     * @param unit  the unit of the period to add, not null
     * @return a {@code ZonedDateTime} based on this date-time with the specified period added, not null
     * @throws DateTimeException if the unit cannot be added to this type
     */
    public ZonedDateTime plus(long amountToAdd, PeriodUnit unit) {
        if (unit instanceof ChronoUnit) {
            return withDateTime(getDateTime().plus(amountToAdd, unit));
        }
        return unit.doPlus(this, amountToAdd);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this {@code ZonedDateTime} with the specified period in years added.
     * <p>
     * This method add the specified amount to the years field in four steps:
     * <ol>
     * <li>Add the input years to the year field</li>
     * <li>Check if the resulting date would be invalid</li>
     * <li>Adjust the day-of-month to the last valid day if necessary</li>
     * <li>Resolve the date-time using {@link ZoneResolvers#retainOffset()}</li>
     * </ol>
     * <p>
     * For example, 2008-02-29 (leap year) plus one year would result in the
     * invalid date 2009-02-29 (standard year). Instead of returning an invalid
     * result, the last valid day of the month, 2009-02-28, is selected instead.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param years  the years to add, positive or negative
     * @return a {@code ZonedDateTime} based on this date-time with the years added, not null
     * @throws DateTimeException if the result exceeds the supported range
     */
    public ZonedDateTime plusYears(long years) {
        LocalDateTime newDT = dateTime.getDateTime().plusYears(years);
        return (newDT == dateTime.getDateTime() ? this :
            resolve(newDT, zone, dateTime, ZoneResolvers.retainOffset()));
    }

    /**
     * Returns a copy of this {@code ZonedDateTime} with the specified period in months added.
     * <p>
     * This method adds the specified amount to the months field in four steps:
     * <ol>
     * <li>Add the input months to the month-of-year field</li>
     * <li>Check if the resulting date would be invalid</li>
     * <li>Adjust the day-of-month to the last valid day if necessary</li>
     * <li>Resolve the date-time using {@link ZoneResolvers#retainOffset()}</li>
     * </ol>
     * <p>
     * For example, 2007-03-31 plus one month would result in the invalid date
     * 2007-04-31. Instead of returning an invalid result, the last valid day
     * of the month, 2007-04-30, is selected instead.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param months  the months to add, positive or negative
     * @return a {@code ZonedDateTime} based on this date-time with the months added, not null
     * @throws DateTimeException if the result exceeds the supported range
     */
    public ZonedDateTime plusMonths(long months) {
        LocalDateTime newDT = dateTime.getDateTime().plusMonths(months);
        return (newDT == dateTime.getDateTime() ? this :
            resolve(newDT, zone, dateTime, ZoneResolvers.retainOffset()));
    }

    /**
     * Returns a copy of this {@code ZonedDateTime} with the specified period in weeks added.
     * <p>
     * This method adds the specified amount in weeks to the days field incrementing
     * the month and year fields as necessary to ensure the result remains valid.
     * The result is only invalid if the maximum/minimum year is exceeded.
     * <p>
     * For example, 2008-12-31 plus one week would result in the 2009-01-07.
     * <p>
     * If the adjustment results in a date-time that is invalid, then the
     * {@link ZoneResolvers#retainOffset()} resolver is used.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param weeks  the weeks to add, positive or negative
     * @return a {@code ZonedDateTime} based on this date-time with the weeks added, not null
     * @throws DateTimeException if the result exceeds the supported range
     */
    public ZonedDateTime plusWeeks(long weeks) {
        LocalDateTime newDT = dateTime.getDateTime().plusWeeks(weeks);
        return (newDT == dateTime.getDateTime() ? this :
            resolve(newDT, zone, dateTime, ZoneResolvers.retainOffset()));
    }

    /**
     * Returns a copy of this {@code ZonedDateTime} with the specified period in days added.
     * <p>
     * This method adds the specified amount to the days field incrementing the
     * month and year fields as necessary to ensure the result remains valid.
     * The result is only invalid if the maximum/minimum year is exceeded.
     * <p>
     * For example, 2008-12-31 plus one day would result in the 2009-01-01.
     * <p>
     * If the adjustment results in a date-time that is invalid, then the
     * {@link ZoneResolvers#retainOffset()} resolver is used.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param days  the days to add, positive or negative
     * @return a {@code ZonedDateTime} based on this date-time with the days added, not null
     * @throws DateTimeException if the result exceeds the supported range
     */
    public ZonedDateTime plusDays(long days) {
        LocalDateTime newDT = dateTime.getDateTime().plusDays(days);
        return (newDT == dateTime.getDateTime() ? this :
            resolve(newDT, zone, dateTime, ZoneResolvers.retainOffset()));
    }

    /**
     * Returns a copy of this {@code ZonedDateTime} with the specified period in hours added.
     * <p>
     * This method uses field based addition.
     * This method changes the field by the specified number of hours.
     * This may, at daylight savings cutover, result in a duration being added
     * that is more or less than the specified number of hours.
     * <p>
     * For example, consider a time-zone where the spring DST cutover means that
     * the local times 01:00 to 01:59 do not exist. Using this method, adding
     * a period of 2 hours to 00:30 will result in 02:30, but it is important
     * to note that the change in duration was only 1 hour.
     * <p>
     * If the adjustment results in a date-time that is invalid, then the
     * {@link ZoneResolvers#retainOffset()} resolver is used.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param hours  the hours to add, positive or negative
     * @return a {@code ZonedDateTime} based on this date-time with the hours added, not null
     * @throws DateTimeException if the result exceeds the supported range
     */
    public ZonedDateTime plusHours(long hours) {
        LocalDateTime newDT = dateTime.getDateTime().plusHours(hours);
        return (newDT == dateTime.getDateTime() ? this :
            resolve(newDT, zone, dateTime, ZoneResolvers.retainOffset()));
    }

    /**
     * Returns a copy of this {@code ZonedDateTime} with the specified period in minutes added.
     * <p>
     * If the adjustment results in a date-time that is invalid, then the
     * {@link ZoneResolvers#retainOffset()} resolver is used.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param minutes  the minutes to add, positive or negative
     * @return a {@code ZonedDateTime} based on this date-time with the minutes added, not null
     * @throws DateTimeException if the result exceeds the supported range
     */
    public ZonedDateTime plusMinutes(long minutes) {
        LocalDateTime newDT = dateTime.getDateTime().plusMinutes(minutes);
        return (newDT == dateTime.getDateTime() ? this :
            resolve(newDT, zone, dateTime, ZoneResolvers.retainOffset()));
    }

    /**
     * Returns a copy of this {@code ZonedDateTime} with the specified period in seconds added.
     * <p>
     * If the adjustment results in a date-time that is invalid, then the
     * {@link ZoneResolvers#retainOffset()} resolver is used.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param seconds  the seconds to add, positive or negative
     * @return a {@code ZonedDateTime} based on this date-time with the seconds added, not null
     * @throws DateTimeException if the result exceeds the supported range
     */
    public ZonedDateTime plusSeconds(long seconds) {
        LocalDateTime newDT = dateTime.getDateTime().plusSeconds(seconds);
        return (newDT == dateTime.getDateTime() ? this :
            resolve(newDT, zone, dateTime, ZoneResolvers.retainOffset()));
    }

    /**
     * Returns a copy of this {@code ZonedDateTime} with the specified period in nanoseconds added.
     * <p>
     * If the adjustment results in a date-time that is invalid, then the
     * {@link ZoneResolvers#retainOffset()} resolver is used.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param nanos  the nanos to add, positive or negative
     * @return a {@code ZonedDateTime} based on this date-time with the nanoseconds added, not null
     * @throws DateTimeException if the result exceeds the supported range
     */
    public ZonedDateTime plusNanos(long nanos) {
        LocalDateTime newDT = dateTime.getDateTime().plusNanos(nanos);
        return (newDT == dateTime.getDateTime() ? this :
            resolve(newDT, zone, dateTime, ZoneResolvers.retainOffset()));
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this {@code ZonedDateTime} with the specified duration added.
     * <p>
     * Adding a duration differs from adding a period as gaps and overlaps in
     * the local time-line are taken into account. For example, if there is a
     * gap in the local time-line of one hour from 01:00 to 02:00, then adding a
     * duration of one hour to 00:30 will yield 02:30.
     * <p>
     * The addition of a duration is always absolute and zone-resolvers are not required.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param hours  the hours to add, positive or negative
     * @param minutes  the minutes to add, positive or negative
     * @param seconds  the seconds to add, positive or negative
     * @param nanos  the nanos to add, positive or negative
     * @return a {@code ZonedDateTime} based on this date-time with the duration added, not null
     * @throws ArithmeticException if the calculation exceeds the capacity of {@code Instant}
     * @throws DateTimeException if the result exceeds the supported range
     */
    public ZonedDateTime plusDuration(int hours, int minutes, int seconds, long nanos) {
        if ((hours | minutes | seconds | nanos) == 0) {
            return this;
        }
        Instant instant = toInstant().plusSeconds(hours * SECONDS_PER_HOUR + minutes * SECONDS_PER_MINUTE + seconds).plusNanos(nanos);
        return ofInstant(instant, zone);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this date-time with the specified period subtracted.
     * <p>
     * This method returns a new date-time based on this time with the specified period subtracted.
     * The adjuster is typically {@link Period} but may be any other type implementing
     * the {@link javax.time.calendrical.DateTime.MinusAdjuster} interface.
     * The calculation is delegated to the specified adjuster, which typically calls
     * back to {@link #minus(long, PeriodUnit)}.
     * <p>
     * If the adjustment results in a date-time that is invalid for the zone,
     * then the {@link ZoneResolvers#retainOffset()} resolver is used.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param adjuster  the adjuster to use, not null
     * @return a {@code ZonedDateTime} based on this date-time with the subtraction made, not null
     * @throws DateTimeException if the subtraction cannot be made
     * @throws ArithmeticException if numeric overflow occurs
     */
    @Override
    public ZonedDateTime minus(MinusAdjuster adjuster) {
        return (ZonedDateTime) adjuster.doMinusAdjustment(this);
    }

    /**
     * Returns a copy of this date-time with the specified period subtracted.
     * <p>
     * This method returns a new date-time based on this date-time with the specified period subtracted.
     * This can be used to subtract any period that is defined by a unit, for example to subtract years, months or days.
     * The unit is responsible for the details of the calculation, including the resolution
     * of any edge cases in the calculation.
     * <p>
     * If the adjustment results in a date-time that is invalid for the zone,
     * then the {@link ZoneResolvers#retainOffset()} resolver is used.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param amountToSubtract  the amount of the unit to subtract from the returned date-time, not null
     * @param unit  the unit of the period to subtract, not null
     * @return a {@code ZonedDateTime} based on this date-time with the specified period subtracted, not null
     * @throws DateTimeException if the unit cannot be added to this type
     */
    @Override
    public ZonedDateTime minus(long amountToSubtract, PeriodUnit unit) {
        return (amountToSubtract == Long.MIN_VALUE ? plus(Long.MAX_VALUE, unit).plus(1, unit) : plus(-amountToSubtract, unit));
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this {@code ZonedDateTime} with the specified period in years subtracted.
     * <p>
     * This method subtracts the specified amount to the years field in four steps:
     * <ol>
     * <li>Add the input years to the year field</li>
     * <li>Check if the resulting date would be invalid</li>
     * <li>Adjust the day-of-month to the last valid day if necessary</li>
     * <li>Resolve the date-time using {@link ZoneResolvers#retainOffset()}</li>
     * </ol>
     * <p>
     * For example, 2008-02-29 (leap year) minus one year would result in the
     * invalid date 2009-02-29 (standard year). Instead of returning an invalid
     * result, the last valid day of the month, 2009-02-28, is selected instead.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param years  the years to subtract, positive or negative
     * @return a {@code ZonedDateTime} based on this date-time with the years subtracted, not null
     * @throws DateTimeException if the result exceeds the supported range
     */
    public ZonedDateTime minusYears(long years) {
        LocalDateTime newDT = dateTime.getDateTime().minusYears(years);
        return (newDT == dateTime.getDateTime() ? this :
            resolve(newDT, zone, dateTime, ZoneResolvers.retainOffset()));
    }

    /**
     * Returns a copy of this {@code ZonedDateTime} with the specified period in months subtracted.
     * <p>
     * This method subtracts the specified amount to the months field in four steps:
     * <ol>
     * <li>Add the input months to the month-of-year field</li>
     * <li>Check if the resulting date would be invalid</li>
     * <li>Adjust the day-of-month to the last valid day if necessary</li>
     * <li>Resolve the date-time using {@link ZoneResolvers#retainOffset()}</li>
     * </ol>
     * <p>
     * For example, 2007-03-31 minus one month would result in the invalid date
     * 2007-04-31. Instead of returning an invalid result, the last valid day
     * of the month, 2007-04-30, is selected instead.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param months  the months to subtract, positive or negative
     * @return a {@code ZonedDateTime} based on this date-time with the months subtracted, not null
     * @throws DateTimeException if the result exceeds the supported range
     */
    public ZonedDateTime minusMonths(long months) {
        LocalDateTime newDT = dateTime.getDateTime().minusMonths(months);
        return (newDT == dateTime.getDateTime() ? this :
            resolve(newDT, zone, dateTime, ZoneResolvers.retainOffset()));
    }

    /**
     * Returns a copy of this {@code ZonedDateTime} with the specified period in weeks subtracted.
     * <p>
     * This method subtracts the specified amount in weeks to the days field incrementing
     * the month and year fields as necessary to ensure the result remains valid.
     * The result is only invalid if the maximum/minimum year is exceeded.
     * <p>
     * For example, 2008-12-31 minus one week would result in the 2009-01-07.
     * <p>
     * If the adjustment results in a date-time that is invalid, then the
     * {@link ZoneResolvers#retainOffset()} resolver is used.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param weeks  the weeks to subtract, positive or negative
     * @return a {@code ZonedDateTime} based on this date-time with the weeks subtracted, not null
     * @throws DateTimeException if the result exceeds the supported range
     */
    public ZonedDateTime minusWeeks(long weeks) {
        LocalDateTime newDT = dateTime.getDateTime().minusWeeks(weeks);
        return (newDT == dateTime.getDateTime() ? this :
            resolve(newDT, zone, dateTime, ZoneResolvers.retainOffset()));
    }

    /**
     * Returns a copy of this {@code ZonedDateTime} with the specified period in days subtracted.
     * <p>
     * This method subtracts the specified amount to the days field incrementing the
     * month and year fields as necessary to ensure the result remains valid.
     * The result is only invalid if the maximum/minimum year is exceeded.
     * <p>
     * For example, 2008-12-31 minus one day would result in the 2009-01-01.
     * <p>
     * If the adjustment results in a date-time that is invalid, then the
     * {@link ZoneResolvers#retainOffset()} resolver is used.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param days  the days to subtract, positive or negative
     * @return a {@code ZonedDateTime} based on this date-time with the days subtracted, not null
     * @throws DateTimeException if the result exceeds the supported range
     */
    public ZonedDateTime minusDays(long days) {
        LocalDateTime newDT = dateTime.getDateTime().minusDays(days);
        return (newDT == dateTime.getDateTime() ? this :
            resolve(newDT, zone, dateTime, ZoneResolvers.retainOffset()));
    }

    /**
     * Returns a copy of this {@code ZonedDateTime} with the specified period in hours subtracted.
     * <p>
     * This method uses field based subtraction.
     * This method changes the field by the specified number of hours.
     * This may, at daylight savings cutover, result in a duration being subtracted
     * that is more or less than the specified number of hours.
     * <p>
     * For example, consider a time-zone where the spring DST cutover means that
     * the local times 01:00 to 01:59 do not exist. Using this method, subtracting
     * a period of 2 hours from 02:30 will result in 00:30, but it is important
     * to note that the change in duration was only 1 hour.
     * <p>
     * If the adjustment results in a date-time that is invalid, then the
     * {@link ZoneResolvers#retainOffset()} resolver is used.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param hours  the hours to subtract, positive or negative
     * @return a {@code ZonedDateTime} based on this date-time with the hours subtracted, not null
     * @throws DateTimeException if the result exceeds the supported range
     */
    public ZonedDateTime minusHours(long hours) {
        LocalDateTime newDT = dateTime.getDateTime().minusHours(hours);
        return (newDT == dateTime.getDateTime() ? this :
            resolve(newDT, zone, dateTime, ZoneResolvers.retainOffset()));
    }

    /**
     * Returns a copy of this {@code ZonedDateTime} with the specified period in minutes subtracted.
     * <p>
     * If the adjustment results in a date-time that is invalid, then the
     * {@link ZoneResolvers#retainOffset()} resolver is used.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param minutes  the minutes to subtract, positive or negative
     * @return a {@code ZonedDateTime} based on this date-time with the minutes subtracted, not null
     * @throws DateTimeException if the result exceeds the supported range
     */
    public ZonedDateTime minusMinutes(long minutes) {
        LocalDateTime newDT = dateTime.getDateTime().minusMinutes(minutes);
        return (newDT == dateTime.getDateTime() ? this :
            resolve(newDT, zone, dateTime, ZoneResolvers.retainOffset()));
    }

    /**
     * Returns a copy of this {@code ZonedDateTime} with the specified period in seconds subtracted.
     * <p>
     * If the adjustment results in a date-time that is invalid, then the
     * {@link ZoneResolvers#retainOffset()} resolver is used.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param seconds  the seconds to subtract, positive or negative
     * @return a {@code ZonedDateTime} based on this date-time with the seconds subtracted, not null
     * @throws DateTimeException if the result exceeds the supported range
     */
    public ZonedDateTime minusSeconds(long seconds) {
        LocalDateTime newDT = dateTime.getDateTime().minusSeconds(seconds);
        return (newDT == dateTime.getDateTime() ? this :
            resolve(newDT, zone, dateTime, ZoneResolvers.retainOffset()));
    }

    /**
     * Returns a copy of this {@code ZonedDateTime} with the specified period in nanoseconds subtracted.
     * <p>
     * If the adjustment results in a date-time that is invalid, then the
     * {@link ZoneResolvers#retainOffset()} resolver is used.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param nanos  the nanos to subtract, positive or negative
     * @return a {@code ZonedDateTime} based on this date-time with the nanoseconds subtracted, not null
     * @throws DateTimeException if the result exceeds the supported range
     */
    public ZonedDateTime minusNanos(long nanos) {
        LocalDateTime newDT = dateTime.getDateTime().minusNanos(nanos);
        return (newDT == dateTime.getDateTime() ? this :
            resolve(newDT, zone, dateTime, ZoneResolvers.retainOffset()));
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this {@code ZonedDateTime} with the specified duration subtracted.
     * <p>
     * Subtracting a duration differs from subtracting a period as gaps and overlaps in
     * the local time-line are taken into account. For example, if there is a
     * gap in the local time-line of one hour from 01:00 to 02:00, then subtracting a
     * duration of one hour from 02:30 will yield 00:30.
     * <p>
     * The subtraction of a duration is always absolute and zone-resolvers are not required.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param hours  the hours to subtract, positive or negative
     * @param minutes  the minutes to subtract, positive or negative
     * @param seconds  the seconds to subtract, positive or negative
     * @param nanos  the nanos to subtract, positive or negative
     * @return a {@code ZonedDateTime} based on this date-time with the duration subtracted, not null
     * @throws ArithmeticException if the calculation exceeds the capacity of {@code Instant}
     * @throws DateTimeException if the result exceeds the supported range
     */
    public ZonedDateTime minusDuration(int hours, int minutes, int seconds, long nanos) {
        if ((hours | minutes | seconds | nanos) == 0) {
            return this;
        }
        Instant instant = toInstant().minusSeconds(hours * SECONDS_PER_HOUR + minutes * SECONDS_PER_MINUTE + seconds).minusNanos(nanos);
        return ofInstant(instant, zone);
    }

    //-----------------------------------------------------------------------
    @Override
    public long periodUntil(DateTime endDateTime, PeriodUnit unit) {
        if (endDateTime instanceof ZonedDateTime == false) {
            throw new DateTimeException("Unable to calculate period between objects of two different types");
        }
        ZonedDateTime end = (ZonedDateTime) endDateTime;
        if (unit instanceof ChronoUnit) {
            OffsetDateTime endODT = end.dateTime.withOffsetSameInstant(dateTime.getOffset());
            return dateTime.periodUntil(endODT, unit);
        }
        return unit.between(this, endDateTime).getAmount();
    }

    //-----------------------------------------------------------------------
    /**
     * Converts this {@code ZonedDateTime} to an {@code OffsetDate}.
     *
     * @return a OffsetDate representing the date fields of this date-time, not null
     */
    public OffsetDate toOffsetDate() {
        return dateTime.toOffsetDate();
    }

    /**
     * Converts this {@code ZonedDateTime} to an {@code OffsetTime}.
     *
     * @return a OffsetTime representing the time fields of this date-time, not null
     */
    public OffsetTime toOffsetTime() {
        return dateTime.toOffsetTime();
    }

    /**
     * Converts this {@code ZonedDateTime} to an {@code OffsetDateTime}.
     *
     * @return a OffsetDateTime representing the fields of this date-time, not null
     */
    @Override
    public OffsetDateTime getOffsetDateTime() {
        return dateTime;
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if this date-time is equal to another date-time.
     * <p>
     * The comparison is based on the offset date-time and the zone.
     * Only objects of type {@code ZonedDateTime} are compared, other types return false.
     *
     * @param obj  the object to check, null returns false
     * @return true if this is equal to the other date-time
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ZonedDateTime) {
            ZonedDateTime other = (ZonedDateTime) obj;
            return dateTime.equals(other.dateTime) &&
                zone.equals(other.zone);
        }
        return false;
    }

    /**
     * A hash code for this date-time.
     *
     * @return a suitable hash code
     */
    @Override
    public int hashCode() {
        return dateTime.hashCode() ^ zone.hashCode();
    }

    //-----------------------------------------------------------------------
    /**
     * Outputs this date-time as a {@code String}, such as
     * {@code 2007-12-03T10:15:30+01:00[Europe/Paris]}.
     * <p>
     * The output will be one of the following formats:
     * <p><ul>
     * <li>{@code yyyy-MM-dd'T'HH:mmXXXXX'['I']'}</li>
     * <li>{@code yyyy-MM-dd'T'HH:mm:ssXXXXX'['I']'}</li>
     * <li>{@code yyyy-MM-dd'T'HH:mm:ssfnnnXXXXX'['I']'}</li>
     * <li>{@code yyyy-MM-dd'T'HH:mm:ssfnnnnnnXXXXX'['I']'}</li>
     * <li>{@code yyyy-MM-dd'T'HH:mm:ssfnnnnnnnnnXXXXX'['I']'}</li>
     * </ul><p>
     * The format used will be the shortest that outputs the full value of
     * the time where the omitted parts are implied to be zero.
     *
     * @return a string representation of this date-time, not null
     */
    @Override  // override for Javadoc
    public String toString() {
        return dateTime.toString() + '[' + zone.toString() + ']';
    }

}
