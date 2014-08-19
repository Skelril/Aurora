/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shard.instance.FreakyFour;

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
import org.bukkit.plugin.RegisteredServiceProvider;

import java.io.IOException;
import java.util.Iterator;

import static com.sk89q.commandbook.CommandBook.inst;
import static com.sk89q.commandbook.CommandBook.registerEvents;
import static com.zachsthings.libcomponents.bukkit.BasePlugin.server;

@ComponentInformation(friendlyName = "Freaky Four", desc = "Freaky Four boss.")
@Depend(components = {AdminComponent.class, PrayerComponent.class})
public class FreakyFour extends ShardComponent<FreakyFourShard, FreakyFourInstance> implements Runnable {

    @InjectComponent
    private AdminComponent admin;
    @InjectComponent
    private ShardManagerComponent manager;

    private FreakyFourConfig config;

    @Override
    public ShardManagerComponent getManager() {
        return manager;
    }

    @Override
    public void start() {
        try {
            shard = new FreakyFourShard(
                    new ShardEditor(
                            new BasicShardSchematic(
                                    "Freaky Four",
                                    manager.getShardWEWorld().getWorldData()
                            )
                    ),
                    config = configure(
                            new FreakyFourConfig()
                    ),
                    admin,
                    manager,
                    getEconomy()
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
        registerEvents(new FreakyFourListener(this));
        server().getScheduler().runTaskTimer(inst(), this, 20, 20);
    }

    @Override
    public void reload() {
        super.reload();
        configure(config);
    }

    private Economy getEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = server().getServicesManager().getRegistration(net.milkbowl
                .vault.economy.Economy.class);
        if (economyProvider != null) {
            return economyProvider.getProvider();
        }
        return null;
    }

    @Override
    public void run() {
        Iterator<FreakyFourInstance> it = instances.iterator();
        while (it.hasNext()) {
            FreakyFourInstance next = it.next();
            if (next.isActive()) {
                next.run();
                continue;
            }
            manager.getManager().unloadInstance(next);
            it.remove();
        }
    }
}
