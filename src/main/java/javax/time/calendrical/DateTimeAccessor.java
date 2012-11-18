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
package javax.time.calendrical;

import javax.time.DateTimeException;
import javax.time.ZoneId;
import javax.time.chrono.Chrono;

/**
 * General low-level access to a date and/or time object.
 * <p>
 * This interface is implemented by all date-time classes.
 * It provides access to the state using the {@link #get(DateTimeField)} and
 * {@link #getLong(DateTimeField)} methods that takes a {@link DateTimeField}.
 * Access is also provided to any additional state using a simple lookup by
 * {@code Class} through {@link #extract(Class)}. This is primarily
 * intended to provide access to the time-zone, offset and calendar system.
 * <p>
 * A sub-interface, {@link DateTime}, extends this definition to one that also
 * supports adjustment and manipulation on more complete date-time objects.
 *
 * <h4>Implementation notes</h4>
 * This interface places no restrictions on implementations and makes no guarantees
 * about their thread-safety.
 * See {@code DateTime} for a full description of whether to implement this
 * interface.
 */
public interface DateTimeAccessor {

    /**
     * Checks if the specified date-time field is supported.
     * <p>
     * This checks if the date-time can be queried for the specified field.
     * If false, then calling the {@link #range(DateTimeField) range} and {@link #get(DateTimeField) get}
     * methods will throw an exception.
     *
     * <h4>Implementation notes</h4>
     * Implementations must check and handle any fields defined in {@link ChronoField} before
     * delegating on to the {@link DateTimeField#doRange(DateTimeAccessor) doRange method} on the specified field.
     *
     * @param field  the field to check, null returns false
     * @return true if this date-time can be queried for the field, false if not
     */
    boolean isSupported(DateTimeField field);

    /**
     * Gets the range of valid values for the specified date-time field.
     * <p>
     * All fields can be expressed as a {@code long} integer.
     * This method returns an object that describes the valid range for that value.
     * If the date-time cannot return the range, because the field is unsupported or for
     * some other reason, an exception will be thrown.
     * <p>
     * Note that the result only describes the minimum and maximum valid values
     * and it is important not to read too much into them. For example, there
     * could be values within the range that are invalid for the field.
     *
     * <h4>Implementation notes</h4>
     * Implementations must check and handle any fields defined in {@link ChronoField} before
     * delegating on to the {@link DateTimeField#doRange(DateTimeAccessor) doRange method} on the specified field.
     *
     * @param field  the field to get, not null
     * @return the range of valid values for the field, not null
     * @throws IllegalArgumentException if the range for the field cannot be obtained
     */
    DateTimeValueRange range(DateTimeField field);

    /**
     * Gets the value of the specified date-time field as an {@code int}.
     * <p>
     * This queries the date-time for the value for the specified field.
     * The returned value will always be within the valid range of values for the field.
     * If the date-time cannot return the value, because the field is unsupported or for
     * some other reason, an exception will be thrown.
     *
     * <h4>Implementation notes</h4>
     * Implementations must check and handle any fields defined in {@link ChronoField} before
     * delegating on to the {@link DateTimeField#doGet(DateTimeAccessor) doGet method} on the specified field.
     *
     * @param field  the field to get, not null
     * @return the value for the field
     * @throws IllegalArgumentException if a value for the field cannot be obtained
     * @throws IllegalArgumentException if the range of valid values for the field exceeds an {@code int}
     * @throws IllegalArgumentException if the value is outside the range of valid values for the field
     * @throws ArithmeticException if numeric overflow occurs
     */
    int get(DateTimeField field);

    /**
     * Gets the value of the specified date-time field as a {@code Long}.
     * <p>
     * This queries the date-time for the value for the specified field.
     * The returned value may be outside the valid range of values for the field.
     * If the date-time cannot return the value, because the field is unsupported or for
     * some other reason, an exception will be thrown.
     *
     * <h4>Implementation notes</h4>
     * Implementations must check and handle any fields defined in {@link ChronoField} before
     * delegating on to the {@link DateTimeField#doGet(DateTimeAccessor) doGet method} on the specified field.
     *
     * @param field  the field to get, not null
     * @return the value for the field
     * @throws IllegalArgumentException if a value for the field cannot be obtained
     * @throws ArithmeticException if numeric overflow occurs
     */
    long getLong(DateTimeField field);

    /**
     * Returns an object of the same type as this object with the specified field altered.
     * <p>
     * This returns a new object based on this one with the value for the specified field changed.
     * For example, on a {@code LocalDate}, this could be used to set the year, month or day-of-month.
     * The returned object will have the same observable type as this object.
     * <p>
     * In some cases, changing a field is not fully defined. For example, if the target object is
     * a date representing the 31st January, then changing the month to February would be unclear.
     * In cases like this, the field is responsible for resolving the result. Typically it will choose
     * the previous valid date, which would be the last valid day of February in this example.
     *
     * <h4>Implementation notes</h4>
     * Implementations must check and handle any fields defined in {@link ChronoField} before
     * delegating on to the {@link DateTimeField#doSet(DateTimeAccessor, long) doSet method} on the specified field.
     * If the implementing class is immutable, then this method must return an updated copy of the original.
     * If the class is mutable, then this method must update the original and return it.
     *
     * @param field  the field to set in the returned date, not null
     * @param newValue  the new value of the field in the returned date, not null
     * @return an object of the same type with the specified field set, not null
     * @throws IllegalArgumentException if the field cannot be set
     * @throws ArithmeticException if numeric overflow occurs
     */
    DateTimeAccessor with(DateTimeField field, long newValue);

    /**
     * Queries this date-time.
     * <p>
     * This queries this date-time using the specified query strategy object.
     * The Query interface defines two constants, {@code Query.ZONE_ID} and {@code Query.CHRONO},
     * which can be used to obtain the {@code ZoneId} and {@code Chrono}.
     * Other queries may be defined by applications.
     *
     * <h4>Implementation notes</h4>
     * The two special constant implementations of {@code Query} must be handled directly.
     * The code must follow a pattern equivalent to the following:
     * <pre>
     *   public &lt;R&gt; R query(Query&lt;R&gt; type) {
     *     if (query == Query.ZONE_ID)  return // either ZoneId or null
     *     if (query == Query.CHRONO)  return // either Chrono or null
     *     return query.doQuery(this);
     *   }
     * </pre>
     *
     * @param <R> the type of the result
     * @param query  the query to invoke, not null
     * @return the query result, null may be returned (defined by the query)
     */
    <R> R query(Query<R> query);

    //-----------------------------------------------------------------------
    /**
     * Strategy for querying a date-time object.
     * <p>
     * This interface allows different kinds of query to be modeled.
     * Examples might be a query that checks if the date is the day before February 29th
     * in a leap year, or calculates the number of days to your next birthday.
     * <p>
     * Implementations should not normally be used directly.
     * Instead, the {@link DateTimeAccessor#query(Query)} method must be used:
     * <pre>
     *   dateTime = dateTime.query(query);
     * </pre>
     * <p>
     * See {@link DateTimeAdjusters} for a standard set of adjusters, including finding the
     * last day of the month.
     *
     * <h4>Implementation notes</h4>
     * This interface must be implemented with care to ensure other classes operate correctly.
     * All implementations that can be instantiated must be final, immutable and thread-safe.
     */
    public interface Query<R> {
        /**
         * The special constant for the query for {@code ZoneId}.
         */
        Query<ZoneId> ZONE_ID = new Query<ZoneId>() {
            @Override
            public ZoneId doQuery(DateTimeAccessor dateTime) {
                throw new IllegalStateException("Cannot invoke dateTime.query(Query.ZONE_ID) directly");
            }
        };
        /**
         * The special constant for the query for {@code Chrono}.
         */
        Query<Chrono<?>> CHRONO = new Query<Chrono<?>>() {
            @Override
            public Chrono<?> doQuery(DateTimeAccessor dateTime) {
                throw new IllegalStateException("Cannot invoke dateTime.query(Query.CHRONO) directly");
            }
        };
        /**
         * Implementation of the strategy to query the specified date-time object.
         * <p>
         * This method is not intended to be called by application code directly.
         * Instead, the {@link DateTimeAccessor#query(Query)} method must be used:
         * <pre>
         *   dateTime = dateTime.query(query);
         * </pre>
         *
         * <h4>Implementation notes</h4>
         * The implementation queries the input date-time object to return the result.
         * For example, an implementation might query the date and time, returning
         * the astronomical Julian day as a {@code BigDecimal}.
         * <p>
         * This interface can be used by calendar systems other than ISO.
         * Implementations may choose to document compatibility with other calendar systems, or
         * validate for it by querying the chronology from the input object.
         *
         * @param dateTime  the date-time object to query, not null
         * @return the queried value, avoid returning null
         * @throws DateTimeException if unable to query  // TODO: What does this mean?
         * @throws ArithmeticException if numeric overflow occurs
         */
        R doQuery(DateTimeAccessor dateTime);
    }

}
