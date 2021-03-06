/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * This file is part of Aurora.
 *
 * Aurora is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Aurora is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with Aurora.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.skelril.aurora.shard.instance.PatientX;

import com.skelril.aurora.admin.AdminComponent;
import com.skelril.aurora.prayer.PrayerComponent;
import com.skelril.aurora.shard.ShardEditor;
import com.skelril.aurora.shard.ShardManagerComponent;
import com.skelril.aurora.shard.instance.BasicShardSchematic;
import com.skelril.aurora.shard.instance.ShardComponent;
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
                    ),
                    admin
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
