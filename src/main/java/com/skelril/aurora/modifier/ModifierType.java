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

package com.skelril.aurora.modifier;

public enum ModifierType {
    DOUBLE_CURSED_ORES("Double Cursed Mine Ores"),
    DOUBLE_WILD_ORES("Double Wilderness Ores"),
    DOUBLE_WILD_DROPS("Double Wilderness Drops"),
    QUAD_GOLD_RUSH("Quadruple Gold Rush"),
    TRIPLE_FACTORY_PRODUCTION("Triple Factory Production"),
    HEXA_FACTORY_SPEED("Hextuple Factory Speed"),
    NONUPLE_MIRAGE_GOLD("Nonuple Mirage Arena Gold");

    final String fname;
    ModifierType(String fname) {
        this.fname = fname;
    }

    public String fname() {
        return fname;
    }
}
