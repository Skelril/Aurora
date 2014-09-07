/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * This file is part of Aurora.
 *
 * Aurora is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Aurora is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with Aurora.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.skelril.aurora.util;

import java.util.Calendar;

/**
 * User: Wyatt Childers
 * Date: 9/20/13
 */
public class TimeUtil {

    private static Calendar calendar = Calendar.getInstance();

    /**
     * Gets the ticks till the start of the next hour
     *
     * @return the number of ticks till the next hour
     */
    public static long getTicksTillHour() {

        Calendar localCalendar = Calendar.getInstance();
        long returnValue;

        localCalendar.set(Calendar.MINUTE, 0);
        localCalendar.add(Calendar.HOUR_OF_DAY, 1);

        returnValue = localCalendar.getTimeInMillis() - calendar.getTimeInMillis();
        returnValue = (returnValue / 1000) * 20; // To Ticks

        return returnValue;
    }

    /**
     * Gets the ticks till a given base 24 hour
     *
     * @param hour The hour, for example 13 is 1 P.M.
     * @return the number of ticks till the given time
     */
    public static long getTicksTill(int hour) {

        return getTicksTill(hour, -1);
    }

    /**
     * Gets the ticks till a given base 24 hour on a day of the week
     *
     * @param hour      The hour, for example 13 is 1 P.M.
     * @param dayofweek The day, for example 7 is Saturday
     * @return the number of ticks till the given time
     */
    public static long getTicksTill(int hour, int dayofweek) {

        Calendar localCalendar = Calendar.getInstance();
        long returnValue;

        localCalendar.set(Calendar.MINUTE, 0);

        while (localCalendar.get(Calendar.HOUR_OF_DAY) != hour) {
            localCalendar.add(Calendar.HOUR_OF_DAY, 1);
        }
        if (dayofweek != -1) {
            while (localCalendar.get(Calendar.DAY_OF_WEEK) != dayofweek) {
                localCalendar.add(Calendar.DAY_OF_WEEK, 1);
            }
        }

        returnValue = localCalendar.getTimeInMillis() - calendar.getTimeInMillis();
        returnValue = (returnValue / 1000) * 20; // To Ticks

        return returnValue;
    }
}
