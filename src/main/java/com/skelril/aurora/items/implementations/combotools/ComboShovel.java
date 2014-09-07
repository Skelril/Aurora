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
