/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.items.implementations.combotools;

import com.sk89q.worldedit.blocks.BlockID;
import com.skelril.aurora.items.generic.AbstractItemFeatureImpl;

import java.util.HashSet;
import java.util.Set;

public class ComboShovel extends AbstractItemFeatureImpl {

    protected static Set<Integer> acceptedTypes = new HashSet<>();

    static {
        acceptedTypes.add(BlockID.DIRT);
        acceptedTypes.add(BlockID.GRASS);
        acceptedTypes.add(BlockID.SAND);
        acceptedTypes.add(BlockID.CLAY);
        acceptedTypes.add(BlockID.MYCELIUM);
        acceptedTypes.add(BlockID.GRAVEL);
        acceptedTypes.add(BlockID.SNOW);
        acceptedTypes.add(BlockID.SNOW_BLOCK);
        acceptedTypes.add(BlockID.SLOW_SAND);
    }
}
