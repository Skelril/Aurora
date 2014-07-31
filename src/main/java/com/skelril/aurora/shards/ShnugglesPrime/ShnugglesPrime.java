/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shards.ShnugglesPrime;

import com.skelril.aurora.admin.AdminComponent;
import com.skelril.aurora.prayer.PrayerComponent;
import com.skelril.aurora.shard.ShardEditor;
import com.skelril.aurora.shards.BasicShardSchematic;
import com.skelril.aurora.shards.ShardComponent;
import com.zachsthings.libcomponents.InjectComponent;

import java.io.IOException;

import static com.sk89q.commandbook.CommandBook.registerEvents;

public class ShnugglesPrime extends ShardComponent<ShnugglesPrimeShard, ShnugglesPrimeInstance> implements Runnable {

    @InjectComponent
    private AdminComponent admin;
    @InjectComponent
    private PrayerComponent prayers;

    @Override
    public void enable() {
        try {
            shard = new ShnugglesPrimeShard(
                    "Shnuggles Prime",
                    new ShardEditor(
                            new BasicShardSchematic(
                                    "Shnuggles Prime",
                                    manager.getShardWEWorld().getWorldData()
                            )
                    ),
                    admin,
                    prayers
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
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
