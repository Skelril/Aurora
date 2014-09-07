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

package com.skelril.aurora.shard;

import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public abstract class ShardInstance<K extends Shard> {

    protected K shard;
    protected World world;
    protected ProtectedRegion region;

    public ShardInstance(K shard, World world, ProtectedRegion region) {
        this.shard = shard;
        this.world = world;
        this.region = region;
    }

    public K getMaster() {
        return shard;
    }

    public World getWorld() {
        return world;
    }
    public ProtectedRegion getRegion() {
        return region;
    }

    public abstract void teleportTo(Player... player);
    public abstract boolean isActive();
    public void prepare() { }
    public void cleanUp() { }
}
