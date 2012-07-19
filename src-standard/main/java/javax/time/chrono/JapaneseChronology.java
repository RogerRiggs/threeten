/*
 * Copyright (c) 2009 Oracle All Rights Reserved.
 */
package javax.time.chrono;

import java.io.Serializable;
import java.util.HashMap;
import javax.time.CalendricalException;
import javax.time.DateTimes;

import javax.time.calendrical.DateTime;
import javax.time.calendrical.LocalDateTimeField;

/**
 * The Japanese Imperial calendar system.
 * <p>
 * {@code JapaneseChronology} defines the rules of the Japanese Imperial calendar system.
 * Only Keio (1865-04-07 - 1868-09-07) and later eras are supported.
 * Older eras are recognized as unknown era, and the year of era of
 * unknown era is Gregorian year.
 * <p>
 * JapaneseChronology is immutable and thread-safe.
 *
 * @author Roger Riggs
 * @author Ryoji Suzuki
 * @author Stephen Colebourne
 */
public final class JapaneseChronology extends Chrono implements Serializable {
    // TODO: Base of GregJulian? Or is ISO sufficient?

    /**
     * A serialization identifier for this class.
     */
    private static final long serialVersionUID = -4760300484384995747L;

    /**
     * The singleton instance of {@code JapaneseChronology}.
     */
    public static final JapaneseChronology INSTANCE = new JapaneseChronology();

    /**
     * Containing the offset from the ISO year.
     */
    static final int YEAR_OFFSET = JapaneseEra.SHOWA.getYearOffset();

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
    private static final String TARGET_LANGUAGE = "ja";

    /**
     * Name data.
     */
    static {
        ERA_NARROW_NAMES.put(FALLBACK_LANGUAGE, new String[]{"Unknown", "K", "M", "T", "S", "H"});
        ERA_NARROW_NAMES.put(TARGET_LANGUAGE, new String[]{"Unknown", "K", "M", "T", "S", "H"});
        ERA_SHORT_NAMES.put(FALLBACK_LANGUAGE, new String[]{"Unknown", "K", "M", "T", "S", "H"});
        ERA_SHORT_NAMES.put(TARGET_LANGUAGE, new String[]{"Unknown", "\u6176", "\u660e", "\u5927", "\u662d", "\u5e73"});
        ERA_FULL_NAMES.put(FALLBACK_LANGUAGE, new String[]{"Unknown", "Keio", "Meiji", "Taisho", "Showa", "Heisei"});
        ERA_FULL_NAMES.put(TARGET_LANGUAGE,
                new String[]{"Unknown", "\u6176\u5fdc", "\u660e\u6cbb", "\u5927\u6b63", "\u662d\u548c", "\u5e73\u6210"});
    }

    //-----------------------------------------------------------------------
    /**
     * Restrictive constructor.
     */
    private JapaneseChronology() {
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
        return "Japanese";
    }

    @Override
    public Era createEra(int eraValue) {
        return JapaneseEra.of(eraValue);
    }

    @Override
    public ChronoDate date(Era era, int yearOfEra, int month, int dayOfMonth) {
        if (era instanceof JapaneseEra) {
            return JapaneseDate.of((JapaneseEra)era, yearOfEra, month, dayOfMonth);
        }
        throw new CalendricalException("Era must be a JapaneseEra");
    }

    @Override
    public ChronoDate date(int prolepticYear, int month, int dayOfMonth) {
        return JapaneseDate.of(prolepticYear, month, dayOfMonth);
    }

    @Override
    public ChronoDate date(DateTime calendrical) {
        long epochDay = calendrical.get(LocalDateTimeField.EPOCH_DAY);
        return dateFromEpochDay(epochDay);
    }

    @Override
    public ChronoDate dateFromEpochDay(long epochDay) {
        return JapaneseDate.ofEpochDay(epochDay);
    }

    @Override
    public boolean isLeapYear(long prolepticYear) {
        return DateTimes.isLeapYear(prolepticYear);
    }

}
