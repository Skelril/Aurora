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

import java.util.Random;

/**
 * @author Turtle9598
 */
public class ChanceUtil {

    private static Random r = new Random(System.currentTimeMillis());

    public static int getRandom(int highestValue) {

        return highestValue == 0 ? 1 : highestValue < 0 ? (r.nextInt(highestValue * -1) + 1) * -1 : r.nextInt(highestValue) + 1;
    }

    public static int getRangedRandom(int lowestValue, int highestValue) {

        if (lowestValue == highestValue) return lowestValue;
        return lowestValue + getRandom((highestValue + 1) - lowestValue) - 1;
    }

    public static double getRandom(double highestValue) {

        if (highestValue < 0) {
            return (r.nextDouble() * (highestValue * -1)) * -1;
        }
        return (r.nextDouble() * (highestValue - 1)) + 1;
    }

    public static double getRangedRandom(double lowestValue, double highestValue) {

        if (lowestValue == highestValue) return lowestValue;
        return lowestValue + getRandom((highestValue + 1) - lowestValue) - 1;
    }

    public static boolean getChance(Number number) {
        return getChance(number.intValue());
    }

    public static boolean getChance(int outOf) {

        return getChance(1, outOf);
    }

    public static boolean getChance(int chance, int outOf) {

        return getRandom(outOf) <= chance;
    }

}
