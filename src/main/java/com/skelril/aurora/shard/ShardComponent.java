/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shard;

import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.zachsthings.libcomponents.ComponentInformation;
import com.zachsthings.libcomponents.bukkit.BukkitComponent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

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

    public void sendToShard(ShardInstance shard, Player... players) {
        BukkitPlayer[] bPlayers = new BukkitPlayer[players.length];
        for (int i = 0; i < players.length; ++i) {
            bPlayers[i] = WE.wrapPlayer(players[i]);
        }
        shard.teleportTo(bPlayers);
    }
}
