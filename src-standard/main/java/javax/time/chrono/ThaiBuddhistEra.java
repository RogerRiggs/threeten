/*
 * Copyright (c) 2009, 2012 Oracle All Rights Reserved.
 */
package javax.time.chrono;

import javax.time.CalendricalException;

/**
 * Defines the valid eras for the Thai Buddhist calendar system.
 * <p>
 * <b>Do not use ordinal() to obtain the numeric representation of a ThaiBuddhistEra
 * instance. Use getValue() instead.</b>
 * <p>
 * ThaiBuddhistEra is immutable and thread-safe.
 *
 * @author Roger Riggs
 * @author Ryoji Suzuki
 * @author Stephen Colebourne
 */
public enum ThaiBuddhistEra implements Era {

    /**
     * The singleton instance for the era before the current one - Before Buddhist -
     * which has the value 0.
     */
    BEFORE_BUDDHIST,
    /**
     * The singleton instance for the current era - Buddhist - which has the value 1.
     */
    BUDDHIST;

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code ThaiBuddhistEra} from a value.
     * <p>
     * The current era (from ISO year -543 onwards) has the value 1
     * The previous era has the value 0.
     *
     * @param thaiBuddhistEra  the era to represent, from 0 to 1
     * @return the ThaiBuddhistEra singleton, never null
     * @throws IllegalCalendarFieldValueException if the era is invalid
     */
    public static ThaiBuddhistEra of(int thaiBuddhistEra) {
        switch (thaiBuddhistEra) {
            case 0:
                return BEFORE_BUDDHIST;
            case 1:
                return BUDDHIST;
            default:
                throw new CalendricalException("Era is not valid for ThaiBuddhistEra");
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the era numeric value.
     * <p>
     * The current era (from ISO year -543 onwards) has the value 1
     * The previous era has the value 0.
     *
     * @return the era value, from 0 (BEFORE_BUDDHIST) to 1 (BUDDHIST)
     */
    @Override
    public int getValue() {
        return ordinal();
    }

}
