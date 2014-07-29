/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shard;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public abstract class Shard {

    private final String name;
    private final ShardEditor editor;
    private int quantity = 0;

    public Shard(String name, ShardEditor editor) {
        this.name = name;
        this.editor = editor;
    }

    public String getName() {
        return name;
    }

    public ShardEditor getEditor() {
        return editor;
    }
    public abstract ShardInstance load(ProtectedRegion region);

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
