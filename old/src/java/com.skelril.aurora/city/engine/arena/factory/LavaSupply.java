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

import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.skelril.aurora.city.engine.arena.AbstractRegionedArena;
import com.skelril.aurora.util.EnvironmentUtil;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public class LavaSupply extends AbstractRegionedArena {

    private ProtectedRegion lava;

    public LavaSupply(World world, ProtectedRegion region, ProtectedRegion lava) {
        super(world, region);
        this.lava = lava;
    }

    // Returns remainder
    public int addLava(int amount) {

        com.sk89q.worldedit.Vector min = lava.getMinimumPoint();
        com.sk89q.worldedit.Vector max = lava.getMaximumPoint();

        int minX = min.getBlockX();
        int minZ = min.getBlockZ();
        int minY = min.getBlockY();
        int maxX = max.getBlockX();
        int maxZ = max.getBlockZ();
        int maxY = max.getBlockY();

        int added = 0;
        for (int y = minY; y <= maxY; ++y) {
            for (int x = minX; x <= maxX; ++x) {
                for (int z = minZ; z <= maxZ; ++z) {
                    if (added < amount) {
                        Block block = getWorld().getBlockAt(x, y, z);
                        if (block.getType() == Material.AIR) {
                            block.setTypeIdAndData(BlockID.STATIONARY_LAVA, (byte) 0, false);
                            ++added;
                        }
                    }
                }
            }
        }
        return amount - added;
    }

    // Returns amount removed
    public int removeLava(int amount) {

        com.sk89q.worldedit.Vector min = lava.getMinimumPoint();
        com.sk89q.worldedit.Vector max = lava.getMaximumPoint();

        int minX = min.getBlockX();
        int minZ = min.getBlockZ();
        int minY = min.getBlockY();
        int maxX = max.getBlockX();
        int maxZ = max.getBlockZ();
        int maxY = max.getBlockY();

        int found = 0;
        for (int y = maxY; y >= minY; --y) {
            for (int x = minX; x <= maxX; ++x) {
                for (int z = minZ; z <= maxZ; ++z) {
                    if (found < amount) {
                        Block block = getWorld().getBlockAt(x, y, z);
                        if (EnvironmentUtil.isLava(block)) {
                            block.setTypeIdAndData(0, (byte) 0, false);
                            ++found;
                        }
                    }
                }
            }
        }
        return found;
    }
}
