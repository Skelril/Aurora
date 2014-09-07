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

package com.skelril.aurora.util.restoration;

import com.sk89q.worldedit.blocks.BaseBlock;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.io.Serializable;

public class BlockRecord implements Comparable<BlockRecord>, Serializable {

    // Location information
    private transient World world = null;
    private final String worldName;
    private final int x, y, z;

    // Block Information
    private final int type, data;

    // Time Information
    private final long time;

    public BlockRecord(Block block) {

        this.world = block.getWorld();
        this.worldName = world.getName();

        this.x = block.getX();
        this.y = block.getY();
        this.z = block.getZ();

        this.type = block.getTypeId();
        this.data = block.getData();
        this.time = System.currentTimeMillis();
    }

    public BlockRecord(Location location, BaseBlock blockData) {

        this.world = location.getWorld();
        this.worldName = world.getName();

        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();

        this.type = blockData.getType();
        this.data = blockData.getData();
        this.time = System.currentTimeMillis();
    }

    public long getTime() {

        return time;
    }

    public void revert() {

        if (world == null) {
            world = Bukkit.getWorld(worldName);
        }

        Block block = world.getBlockAt(x, y, z);
        Chunk chunk = block.getChunk();
        if (!chunk.isLoaded()) {
            chunk.load();
        }

        block.setTypeIdAndData(type, (byte) data, true);
    }

    // Oldest to newest
    @Override
    public int compareTo(BlockRecord record) {

        if (record == null) return -1;

        if (this.getTime() == record.getTime()) return 0;
        if (this.getTime() > record.getTime()) return 1;
        return -1;
    }
}
