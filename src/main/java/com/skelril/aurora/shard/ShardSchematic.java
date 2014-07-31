/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shard;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.session.ClipboardHolder;

import java.io.IOException;

public interface ShardSchematic {
    public ClipboardHolder getHolder() throws IOException;
    public Vector getDimensions();
}
