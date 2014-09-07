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

package com.skelril.aurora.city.engine.arena.factory;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.skelril.aurora.city.engine.arena.AbstractRegionedArena;
import com.skelril.aurora.util.CollectionUtil;
import org.bukkit.World;

public class AbstractFactoryArea extends AbstractRegionedArena {

    protected ProtectedRegion potionChamber;
    protected ProtectedRegion[] smeltingChamber;

    public AbstractFactoryArea(World world, ProtectedRegion region,
                               ProtectedRegion potionChamber, ProtectedRegion[] smeltingChamer) {
        super(world, region);
        this.potionChamber = potionChamber;
        this.smeltingChamber = smeltingChamer;
    }

    public ProtectedRegion getChamber(ChamberType type) {
        switch (type) {
            case POTION:
                return potionChamber;
            case SMELTING:
                return CollectionUtil.getElement(smeltingChamber);
        }
        return null;
    }

    protected enum ChamberType {
        POTION,
        SMELTING
    }
}
