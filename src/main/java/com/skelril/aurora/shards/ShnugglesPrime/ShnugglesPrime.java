/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shards.ShnugglesPrime;

import com.skelril.aurora.admin.AdminComponent;
import com.skelril.aurora.prayer.PrayerComponent;
import com.skelril.aurora.shard.ShardEditor;
import com.skelril.aurora.shard.ShardManagerComponent;
import com.skelril.aurora.shards.BasicShardSchematic;
import com.skelril.aurora.shards.ShardComponent;
import com.zachsthings.libcomponents.ComponentInformation;
import com.zachsthings.libcomponents.Depend;
import com.zachsthings.libcomponents.InjectComponent;

import java.io.IOException;
import java.util.Iterator;

import static com.sk89q.commandbook.CommandBook.inst;
import static com.sk89q.commandbook.CommandBook.registerEvents;
import static com.zachsthings.libcomponents.bukkit.BasePlugin.server;

@ComponentInformation(friendlyName = "Shnuggles Prime", desc = "Shnuggles Prime Boss.")
@Depend(components = {AdminComponent.class, PrayerComponent.class})
public class ShnugglesPrime extends ShardComponent<ShnugglesPrimeShard, ShnugglesPrimeInstance> implements Runnable {

    @InjectComponent
    private AdminComponent admin;
    @InjectComponent
    private PrayerComponent prayers;
    @InjectComponent
    protected ShardManagerComponent manager;

    @Override
    public ShardManagerComponent getManager() {
        return manager;
    }

    @Override
    public void start() {
        try {
            shard = new ShnugglesPrimeShard(
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
        server().getScheduler().runTaskTimer(inst(), this, 20, 20);
    }

    @Override
    public void run() {
        Iterator<ShnugglesPrimeInstance> it = instances.iterator();
        while (it.hasNext()) {
            ShnugglesPrimeInstance next = it.next();
            if (next.isActive()) {
                next.run();
                continue;
            }
            manager.getManager().unloadInstance(next);
            it.remove();
        }
    }
}
