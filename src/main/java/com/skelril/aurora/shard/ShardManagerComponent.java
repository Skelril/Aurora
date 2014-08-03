/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shard;

import com.sk89q.commandbook.CommandBook;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.zachsthings.libcomponents.ComponentInformation;
import com.zachsthings.libcomponents.bukkit.BukkitComponent;
import org.bukkit.Bukkit;
import org.bukkit.World;

@ComponentInformation(friendlyName = "Shard Instance Manager", desc = "Shard Instancing")
public class ShardManagerComponent extends BukkitComponent {

    private WorldGuardPlugin WG = WGBukkit.getPlugin();
    private ShardManager manager;
    private BukkitWorld shardWorld;

    @Override
    public void enable() {
        CommandBook.server().getScheduler().runTaskLater(CommandBook.inst(), () -> {
            shardWorld = new BukkitWorld(Bukkit.getWorld("Exemplar"));
            manager = new ShardManager(shardWorld, WG.getRegionManager(shardWorld.getWorld()));
        }, 1);
    }

    public ShardManager getManager() {
        return manager;
    }

    public World getShardWorld() {
        return shardWorld.getWorld();
    }

    public com.sk89q.worldedit.world.World getShardWEWorld() {
        return shardWorld;
    }
}
