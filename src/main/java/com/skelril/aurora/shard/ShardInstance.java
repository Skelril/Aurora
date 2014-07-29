/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shard;

import com.sk89q.worldedit.entity.Player;

public interface ShardInstance {
    public Shard getMaster();
    public String getName();

    public void teleportTo(Player... player);
}
