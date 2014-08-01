/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
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
    public abstract void cleanUp();
}
