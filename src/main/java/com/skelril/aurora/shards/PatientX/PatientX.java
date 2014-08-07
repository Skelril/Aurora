/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shards.PatientX;

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

@ComponentInformation(friendlyName = "Patient X", desc = "Patient X Boss.")
@Depend(components = {AdminComponent.class, PrayerComponent.class})
public class PatientX extends ShardComponent<PatientXShard, PatientXInstance> implements Runnable {

    @InjectComponent
    private AdminComponent admin;
    @InjectComponent
    private PrayerComponent prayers;
    @InjectComponent
    private ShardManagerComponent manager;

    private PatientXConfig config;

    @Override
    public ShardManagerComponent getManager() {
        return manager;
    }

    @Override
    public void start() {
        try {
            shard = new PatientXShard(
                    new ShardEditor(
                            new BasicShardSchematic(
                                    "Patient X",
                                    manager.getShardWEWorld().getWorldData()
                            )
                    ),
                    config = configure(
                            new PatientXConfig()
                    )
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
        registerEvents(new PatientXListener(this));
        server().getScheduler().runTaskTimer(inst(), this, 20, 20);
    }

    @Override
    public void reload() {
        super.reload();
        configure(config);
    }

    @Override
    public void run() {
        Iterator<PatientXInstance> it = instances.iterator();
        while (it.hasNext()) {
            PatientXInstance next = it.next();
            if (next.isActive()) {
                next.run();
                continue;
            }
            manager.getManager().unloadInstance(next);
            it.remove();
        }
    }
}
