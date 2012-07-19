/*
 * Copyright (c) 2009 Oracle All Rights Reserved.
 */
package javax.time.chrono;

import javax.time.CalendricalException;

/**
 * Defines the valid eras for the Hijrah calendar system.
 * <p>
 * <b>Do not use ordinal() to obtain the numeric representation of a HijrahEra
 * instance. Use getValue() instead.</b>
 * <p>
 * HijrahEra is immutable and thread-safe.
 *
 * @author Roger Riggs
 * @author Ryoji Suzuki
 * @author Stephen Colebourne
 */
public enum HijrahEra implements Era {

    /**
     * The singleton instance for the era before the current one - Before Hijrah -
     * which has the value 0.
     */
    BEFORE_HIJRAH,
    /**
     * The singleton instance for the current era - Hijrah - which has the value 1.
     */
    HIJRAH;

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code HijrahEra} from a value.
     * <p>
     * The current era (from ISO date 622-06-19 onwards) has the value 1
     * The previous era has the value 0.
     *
     * @param hijrahEra  the era to represent, from 0 to 1
     * @return the HijrahEra singleton, never null
     * @throws CalendricalException if the era is invalid
     */
    public static HijrahEra of(int hijrahEra) {
        switch (hijrahEra) {
            case 0:
                return BEFORE_HIJRAH;
            case 1:
                return HIJRAH;
            default:
                throw new CalendricalException("HijrahEra not valid");
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the era numeric value.
     * <p>
     * The current era (from ISO date 622-06-19 onwards) has the value 1.
     * The previous era has the value 0.
     *
     * @return the era value, from 0 (BEFORE_HIJRAH) to 1 (HIJRAH)
     */
    @Override
    public int getValue() {
        return ordinal();
    }

}
