/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shards;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.entity.Player;
import com.skelril.aurora.shard.Shard;
import com.skelril.aurora.shard.ShardInstance;
import com.skelril.aurora.shard.ShardManagerComponent;
import com.skelril.aurora.util.LocationUtil;
import com.zachsthings.libcomponents.InjectComponent;
import com.zachsthings.libcomponents.TemplateComponent;
import com.zachsthings.libcomponents.bukkit.BukkitComponent;
import org.bukkit.Location;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@TemplateComponent
public abstract class ShardComponent<A extends Shard<T>, T extends ShardInstance> extends BukkitComponent {

    protected WorldEditPlugin WE;

    @InjectComponent
    protected ShardManagerComponent manager;

    protected A shard;
    protected Set<T> instances = new HashSet<>();

    public A getShard() {
        return shard;
    }

    public Set<T> getInstances() {
        return instances;
    }

    public boolean matchesShard(String name) {
        return shard.getName().equals(name);
    }

    public T makeInstance() {
        T instance = manager.getManager().findOrCreateInstance(shard);
        instances.add(instance);
        return instance;
    }

    public T getInstance(Location location) {
        for (T instance : instances) {
            if (LocationUtil.isInRegion(instance.getRegion(), location)) {
                return instance;
            }
        }
        return null;
    }

    public Player[] wrapPlayers(List<org.bukkit.entity.Player> players) {
        Player[] result = new Player[players.size()];
        for (int i = 0; i < result.length; ++i) {
            result[i] = WE.wrapPlayer(players.get(i));
        }
        return result;
    }
}
