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

public class ComboAxe extends AbstractItemFeatureImpl {

    protected static Set<Integer> acceptedTypes = new HashSet<>();

    static {
        acceptedTypes.add(BlockID.LOG);
        acceptedTypes.add(BlockID.LOG2);
        acceptedTypes.add(BlockID.WOOD);
        acceptedTypes.add(BlockID.WOODEN_STEP);
        acceptedTypes.add(BlockID.DOUBLE_WOODEN_STEP);
        acceptedTypes.add(BlockID.OAK_WOOD_STAIRS);
        acceptedTypes.add(BlockID.DARK_OAK_STAIRS);
        acceptedTypes.add(BlockID.BIRCH_WOOD_STAIRS);
        acceptedTypes.add(BlockID.SPRUCE_WOOD_STAIRS);
        acceptedTypes.add(BlockID.JUNGLE_WOOD_STAIRS);
    }
}
