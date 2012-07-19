/*
 * Copyright (c) 2012, Oracle Inc.  All Rights Reserved
 *
 * 
 */
package javax.time.chrono;

import java.util.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import org.testng.Assert;

/**
 * Test Chrono class.
 */
@Test
public class TestChrono {
    //-----------------------------------------------------------------------
    // regular data factory for names and descriptions of available calendars
    //-----------------------------------------------------------------------
    @DataProvider(name = "Calendars")
    Object[][] data_of_calendars() {
        return new Object[][] {
                    {"Coptic", null, "Coptic calendar"},
                    {"Hijrah", null, "Hijrah calendar"},
                    {"ISO", null, "ISO calendar"},
                    {"Japanese", null, "Japanese calendar"},
                    {"Minguo", null, "Minguo Calendar"}, //          {"Islamic", null, "Islamic"},
                    {"ThaiBuddhist", null, "Thai Buddhist calendar"},
                //          {"Chinese", null, "Traditional Chinese calendar" },
                //          {"Ethioaa", "ethiopic-amete-alem", "Ethiopic calendar, Amete Alem (epoch approx. 5493 B.C.E)" },
                //          {"Ethiopic", null, "Ethiopic calendar, Amete Mihret (epoch approx, 8 C.E.)" },
                //          {"Hebrew", null, "Traditional Hebrew calendar" },
                };
    }

    @BeforeMethod
    public void setUp() {
        // Ensure each of the classes are initialized (until initialization is fixed)
        Chrono c;
        c = CopticChrono.INSTANCE;
        c = HijrahChronology.INSTANCE;
        c = ISOChrono.INSTANCE;
        c = JapaneseChronology.INSTANCE;
        c = MinguoChrono.INSTANCE;
        c = ThaiBuddhistChronology.INSTANCE;
    }

    @Test(dataProvider = "Calendars")
    public void test_required_calendars(String name, String alias, String description) {
        Chrono chrono = Chrono.ofName(name);
        Assert.assertNotNull(chrono, "Required calendar not found: " + name);
        Set<String> cals = Chrono.getAvailableNames();
        Assert.assertTrue(cals.contains(name), "Required calendar not found in set of available calendars");
    }

    @Test()
    public void test_calendar_list() {
        Set<String> names = Chrono.getAvailableNames();
        Assert.assertNotNull(names, "Required list of calendars must be non-null");
        for (String name : names) {
            Chrono chrono = Chrono.ofName(name);
            Assert.assertNotNull(chrono, "Required calendar not found: " + name);
        }
        Assert.assertEquals(names.size(), 6, "Required list of calendars too short");
    }


    /**
     * Compute the number of days from the Epoch and compute the date from the number of days.
     */
    @Test(dataProvider = "Calendars")
    public void test_epoch(String name, String alias, String description) {
        Chrono chrono = Chrono.ofName(name); // a chronology. In practice this is rarely hardcoded
        ChronoDate date1 = chrono.now();
        long epoch1 = date1.toEpochDay();
        ChronoDate date2 = chrono.dateFromEpochDay(epoch1);
        Assert.assertEquals(date1, date2, "Date from epoch day is not same date: " + date1 + " != " + date2);
        long epoch2 = date1.toEpochDay();
        Assert.assertEquals(epoch1, epoch2, "Epoch day not the same: " + epoch1 + " != " + epoch2);
    }

}