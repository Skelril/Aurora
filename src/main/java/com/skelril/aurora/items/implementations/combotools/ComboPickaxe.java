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

public class ComboPickaxe extends AbstractItemFeatureImpl {

    protected static Set<Integer> acceptedTypes = new HashSet<>();

    static {
        acceptedTypes.add(BlockID.STONE);
        acceptedTypes.add(BlockID.SANDSTONE);
        acceptedTypes.add(BlockID.SANDSTONE_STAIRS);
        acceptedTypes.add(BlockID.LIGHTSTONE);
        acceptedTypes.add(BlockID.COBBLESTONE);
        acceptedTypes.add(BlockID.COBBLESTONE_STAIRS);
        acceptedTypes.add(BlockID.COBBLESTONE_WALL);
        acceptedTypes.add(BlockID.MOSSY_COBBLESTONE);
        acceptedTypes.add(BlockID.STONE_BRICK);
        acceptedTypes.add(BlockID.STONE_BRICK_STAIRS);
        acceptedTypes.add(BlockID.BRICK);
        acceptedTypes.add(BlockID.BRICK_STAIRS);
        acceptedTypes.add(BlockID.NETHERRACK);
        acceptedTypes.add(BlockID.NETHER_BRICK);
        acceptedTypes.add(BlockID.NETHER_BRICK_STAIRS);
        acceptedTypes.add(BlockID.NETHER_BRICK_FENCE);
        acceptedTypes.add(BlockID.STEP);
        acceptedTypes.add(BlockID.DOUBLE_STEP);
        acceptedTypes.add(BlockID.OBSIDIAN);
    }
}
