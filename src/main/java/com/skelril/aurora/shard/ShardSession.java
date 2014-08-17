/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shard;

import com.sk89q.commandbook.session.PersistentSession;

import java.lang.ref.WeakReference;

public class ShardSession extends PersistentSession {

    private WeakReference<ShardInstance<?>> lastInstance = new WeakReference<>(null);

    protected ShardSession() {
        super(ONE_HOUR);
    }

    public ShardInstance<?> getLastInstance() {
        return lastInstance.get();
    }

    public void setLastInstance(ShardInstance<?> instance) {
        lastInstance = new WeakReference<>(instance);
    }
}
