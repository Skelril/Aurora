/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shard;

import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public abstract class Shard<T extends ShardInstance> {

    private final ShardType shard;
    private final ShardEditor editor;
    private int quantity = 0;

    public Shard(ShardType shard, ShardEditor editor) {
        this.shard = shard;
        this.editor = editor;
    }

    public ShardType getType() {
        return shard;
    }

    public String getRGName() {
        return shard.getName().toLowerCase().replace(" ", "-");
    }

    public ShardEditor getEditor() {
        return editor;
    }
    public abstract T load(World world, ProtectedRegion region);

    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    public void modifyQuantity(int quantity) {
        this.quantity += quantity;
    }
}
