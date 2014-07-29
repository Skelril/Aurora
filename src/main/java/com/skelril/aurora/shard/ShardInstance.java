/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shard;

import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public abstract class ShardInstance {

    protected Shard shard;
    protected World world;
    protected ProtectedRegion region;

    public ShardInstance(Shard shard, World world, ProtectedRegion region) {
        this.shard = shard;
        this.world = world;
        this.region = region;
    }

    public Shard getMaster() {
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
}
