/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shard;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public interface Shard {
    public String getName();

    public ShardEditor getEditor();
    public ShardInstance load(ProtectedRegion region);

    public int getQuantity();
    public void setQuantity(int amt);
    public void modifyQuantity(int amt);
}
