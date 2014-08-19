/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
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
