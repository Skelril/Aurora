/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shards;

import org.bukkit.event.Listener;

public class ShardListener<S extends ShardComponent> implements Listener {

    protected S shard;

    public ShardListener(S shard) {
        this.shard = shard;
    }
}
