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

package com.skelril.aurora.shard.instance.GoldRush;


import com.skelril.aurora.admin.AdminComponent;
import com.skelril.aurora.prayer.PrayerComponent;
import com.skelril.aurora.shard.ShardEditor;
import com.skelril.aurora.shard.ShardManagerComponent;
import com.skelril.aurora.shard.instance.BasicShardSchematic;
import com.skelril.aurora.shard.instance.ShardComponent;
import com.zachsthings.libcomponents.ComponentInformation;
import com.zachsthings.libcomponents.Depend;
import com.zachsthings.libcomponents.InjectComponent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.io.IOException;
import java.util.Iterator;

import static com.sk89q.commandbook.CommandBook.inst;
import static com.sk89q.commandbook.CommandBook.registerEvents;
import static com.zachsthings.libcomponents.bukkit.BasePlugin.server;

@ComponentInformation(friendlyName = "Gold Rush", desc = "Gold Rush minigame.")
@Depend(components = {AdminComponent.class, PrayerComponent.class})
public class GoldRush extends ShardComponent<GoldRushShard, GoldRushInstance> implements Runnable {

    @InjectComponent
    private AdminComponent admin;
    @InjectComponent
    private ShardManagerComponent manager;

    @Override
    public ShardManagerComponent getManager() {
        return manager;
    }

    @Override
    public void start() {
        try {
            shard = new GoldRushShard(
                    new ShardEditor(
                            new BasicShardSchematic(
                                    "Gold Rush",
                                    manager.getShardWEWorld().getWorldData()
                            )
                    ),
                    admin,
                    manager,
                    getEconomy()
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
        registerEvents(new GoldRushListener(this));
        server().getScheduler().runTaskTimer(inst(), this, 20, 20);
    }

    private Economy getEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = server().getServicesManager().getRegistration(net.milkbowl
                .vault.economy.Economy.class);
        if (economyProvider != null) {
            return economyProvider.getProvider();
        }
        return null;
    }

    public void clearAndRefund() {
        for (GoldRushInstance inst : instances) {
            inst.getContained(Player.class).forEach(inst::refundPlayer);
        }
    }

    @Override
    public void run() {
        Iterator<GoldRushInstance> it = instances.iterator();
        while (it.hasNext()) {
            GoldRushInstance next = it.next();
            if (next.isActive()) {
                next.run();
                continue;
            }
            manager.getManager().unloadInstance(next);
            it.remove();
        }
    }
}
