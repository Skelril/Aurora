/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shard;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.zachsthings.libcomponents.ComponentInformation;
import com.zachsthings.libcomponents.bukkit.BukkitComponent;
import org.bukkit.Bukkit;
import org.bukkit.World;

@ComponentInformation(friendlyName = "Shard Instance", desc = "Shard Instancing")
public class ShardComponent extends BukkitComponent {

    private WorldEditPlugin WE;
    private WorldGuardPlugin WG;
    private ShardManager manager;

    @Override
    public void enable() {
        World bWorld = Bukkit.getWorld("Dungeon");
        BukkitWorld world = new BukkitWorld(bWorld);
        manager = new ShardManager(world, WG.getRegionManager(bWorld));
    }

    public ShardManager getManager() {
        return manager;
    }
}
