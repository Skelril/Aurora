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
