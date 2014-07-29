/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shard;

import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public abstract class ShardInstance {

    protected Shard shard;
    protected ProtectedRegion region;

    public ShardInstance(Shard shard, ProtectedRegion region) {
        this.shard = shard;
        this.region = region;
    }

    public Shard getMaster() {
        return shard;
    }

    public ProtectedRegion getRegion() {
        return region;
    }

    public abstract void teleportTo(Player... player);
}
