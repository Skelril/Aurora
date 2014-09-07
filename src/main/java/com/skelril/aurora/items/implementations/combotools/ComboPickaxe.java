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
