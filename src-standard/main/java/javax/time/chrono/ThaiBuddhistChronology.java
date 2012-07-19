/*
 * Copyright (c) 2009, 2012 Oracle All Rights Reserved.
 */
package javax.time.chrono;

import java.io.Serializable;
import java.util.HashMap;
import javax.time.CalendricalException;
import javax.time.DateTimes;
import javax.time.LocalDate;
import javax.time.calendrical.DateTime;

/**
 * The Thai Buddhist calendar system.
 * <p>
 * {@code ThaiBuddhistChronology} defines the rules of the Thai Buddhist calendar system.
 * <p>
 * The Thai Buddhist calendar system is the same as the ISO calendar system apart from the year.
 * <p>
 * ThaiBuddhistChronology is immutable and thread-safe.
 *
 * @author Roger Riggs
 * @author Ryoji Suzuki
 * @author Stephen Colebourne
 */
public final class ThaiBuddhistChronology extends Chrono implements Serializable {

    /**
     * A serialization identifier for this class.
     */
    private static final long serialVersionUID = 5856454970865881985L;

    /**
     * The singleton instance of {@code ThaiBuddhistChronology}.
     */
    public static final ThaiBuddhistChronology INSTANCE = new ThaiBuddhistChronology();

    /**
     * Containing the offset from the ISO year.
     */
    static final int YEAR_OFFSET = -543;

    /**
     * Narrow names for eras.
     */
    private static final HashMap<String, String[]> ERA_NARROW_NAMES = new HashMap<String, String[]>();

    /**
     * Short names for eras.
     */
    private static final HashMap<String, String[]> ERA_SHORT_NAMES = new HashMap<String, String[]>();

    /**
     * Full names for eras.
     */
    private static final HashMap<String, String[]> ERA_FULL_NAMES = new HashMap<String, String[]>();

    /**
     * Fallback language for the era names.
     */
    private static final String FALLBACK_LANGUAGE = "en";

    /**
     * Language that has the era names.
     */
    private static final String TARGET_LANGUAGE = "th";

    /**
     * Name data.
     */
    static {
        ERA_NARROW_NAMES.put(FALLBACK_LANGUAGE, new String[]{"BB", "BE"});
        ERA_NARROW_NAMES.put(TARGET_LANGUAGE, new String[]{"BB", "BE"});
        ERA_SHORT_NAMES.put(FALLBACK_LANGUAGE, new String[]{"B.B.", "B.E."});
        ERA_SHORT_NAMES.put(TARGET_LANGUAGE,
                new String[]{"\u0e1e.\u0e28.",
                "\u0e1b\u0e35\u0e01\u0e48\u0e2d\u0e19\u0e04\u0e23\u0e34\u0e2a\u0e15\u0e4c\u0e01\u0e32\u0e25\u0e17\u0e35\u0e48"});
        ERA_FULL_NAMES.put(FALLBACK_LANGUAGE, new String[]{"Before Buddhist", "Budhhist Era"});
        ERA_FULL_NAMES.put(TARGET_LANGUAGE,
                new String[]{"\u0e1e\u0e38\u0e17\u0e18\u0e28\u0e31\u0e01\u0e23\u0e32\u0e0a",
                "\u0e1b\u0e35\u0e01\u0e48\u0e2d\u0e19\u0e04\u0e23\u0e34\u0e2a\u0e15\u0e4c\u0e01\u0e32\u0e25\u0e17\u0e35\u0e48"});
    }

    //-----------------------------------------------------------------------
    /**
     * Restrictive constructor.
     */
    private ThaiBuddhistChronology() {
    }

    /**
     * Resolves singleton.
     *
     * @return the singleton instance
     */
    private Object readResolve() {
        return INSTANCE;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the name of the chronology.
     *
     * @return the name of the chronology, never null
     */
    @Override
    public String getName() {
        return "ThaiBuddhist";
    }

    @Override
    public Era createEra(int eraValue) {
        return ThaiBuddhistEra.of(eraValue);
    }

    @Override
    public ChronoDate date(Era era, int yearOfEra, int month, int dayOfMonth) {
        if (era instanceof ThaiBuddhistEra) {
            return ThaiBuddhistDate.of((ThaiBuddhistEra)era, yearOfEra, month, dayOfMonth);
        }
        throw new CalendricalException("Era must be a ThaiBuddhistEra");
    }

    @Override
    public ChronoDate date(int prolepticYear, int month, int dayOfMonth) {
        return ThaiBuddhistDate.of(prolepticYear, month, dayOfMonth);
    }

    @Override
    public ChronoDate date(DateTime calendrical) {
        return ThaiBuddhistDate.from(calendrical);
    }

    @Override
    public ChronoDate dateFromEpochDay(long epochDay) {
        return ThaiBuddhistDate.ofEpochDay(epochDay);
    }

    @Override
    public boolean isLeapYear(long prolepticYear) {
        return DateTimes.isLeapYear(prolepticYear);
    }

}
