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

package com.skelril.aurora.homes;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.skelril.aurora.util.checker.Expression;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.Map;

public class PlotOutliner {

    private final Map<BaseBlock, BaseBlock> mapping;
    private final Expression<BaseBlock, Boolean> expr;

    public PlotOutliner(Map<BaseBlock, BaseBlock> mapping, Expression<BaseBlock, Boolean> expr) {
        this.mapping = mapping;
        this.expr = expr;
    }

    public void outline(World world, ProtectedRegion region) {
        edit(world, region, false);
    }

    public void revert(World world, ProtectedRegion region) {
        edit(world, region, true);
    }

    private void edit(World world, ProtectedRegion region, boolean revert) {
        BlockVector min = region.getMinimumPoint();
        BlockVector max = region.getMaximumPoint();

        for (int x = min.getBlockX(); x <= max.getBlockX(); ++x) {
            setBlock(world, revert, x, min.getBlockZ());
            setBlock(world, revert, x, max.getBlockZ());
        }

        for (int z = min.getBlockZ(); z <= max.getBlockZ(); ++z) {
            setBlock(world, revert, min.getBlockX(), z);
            setBlock(world, revert, max.getBlockX(), z);
        }
    }

    private void setBlock(World world, boolean revert, int x, int z) {
        for (int y = world.getMaxHeight(); y > 1; --y) {

            Block target = world.getBlockAt(x, y, z);
            Block below = target.getRelative(BlockFace.DOWN);

            BaseBlock tBase = new BaseBlock(target.getTypeId(), target.getData());
            BaseBlock bBase = new BaseBlock(below.getTypeId(), below.getData());

            for (Map.Entry<BaseBlock, BaseBlock> entry : mapping.entrySet()) {
                BaseBlock from;
                BaseBlock to;
                if (!revert) {
                    from = entry.getKey();
                    to = entry.getValue();
                } else {
                    from = entry.getValue();
                    to = entry.getKey();
                }

                if (tBase.equals(from) && expr.evaluate(bBase)) {
                    target.setTypeIdAndData(to.getType(), (byte) to.getData(), true);
                    return;
                }
            }
        }
    }
}
