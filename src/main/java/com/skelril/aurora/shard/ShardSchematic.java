/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shard;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.session.ClipboardHolder;

public interface ShardSchematic {
    public ClipboardHolder getHolder();
    public Vector getDimensions();
}
