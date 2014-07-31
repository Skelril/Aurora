/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shards.ShnugglesPrime;

import com.skelril.aurora.shards.ShardComponent;

import static com.sk89q.commandbook.CommandBook.registerEvents;

public class ShnugglesPrime extends ShardComponent<ShnugglesPrimeShard, ShnugglesPrimeInstance> implements Runnable {

    @Override
    public void enable() {
        // TOOD Add a editor
        shard = new ShnugglesPrimeShard("Shnuggles Prime", null);
        registerEvents(new ShnugglesPrimeListener(this));
    }

    @Override
    public void run() {
        for (ShnugglesPrimeInstance instance : instances) {
            if (instance.isActive()) {
                instance.run();
            } else {
                manager.getManager().unloadInstance(instance);
            }
        }
    }
}
