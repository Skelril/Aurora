/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shards;

import com.sk89q.commandbook.CommandBook;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.entity.Player;
import com.skelril.aurora.exceptions.UnknownPluginException;
import com.skelril.aurora.shard.Shard;
import com.skelril.aurora.shard.ShardInstance;
import com.skelril.aurora.shard.ShardManagerComponent;
import com.skelril.aurora.shard.ShardType;
import com.skelril.aurora.util.LocationUtil;
import com.zachsthings.libcomponents.TemplateComponent;
import com.zachsthings.libcomponents.bukkit.BukkitComponent;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.zachsthings.libcomponents.bukkit.BasePlugin.server;

@TemplateComponent
public abstract class ShardComponent<A extends Shard<T>, T extends ShardInstance> extends BukkitComponent {

    protected WorldEditPlugin WE;

    protected A shard;
    protected Set<T> instances = new HashSet<>();

    @Override
    public void enable() {
        try {
            setUpWorldEdit();
        } catch (UnknownPluginException e) {
            e.printStackTrace();
        }
        server().getScheduler().runTaskLater(CommandBook.inst(), this::start, 2);
    }

    private void setUpWorldEdit() throws UnknownPluginException {
        Plugin plugin = server().getPluginManager().getPlugin("WorldEdit");

        // WorldEdit may not be loaded
        if (plugin == null || !(plugin instanceof WorldEditPlugin)) {
            throw new UnknownPluginException("WorldEdit");
        }

        WE = (WorldEditPlugin) plugin;
    }

    public abstract ShardManagerComponent getManager();
    public abstract void start();

    public A getShard() {
        return shard;
    }

    public Set<T> getInstances() {
        return instances;
    }

    public boolean matchesShard(ShardType aShard) {
        return shard.getType().equals(aShard);
    }

    public T makeInstance() {
        T instance = getManager().getManager().findOrCreateInstance(shard);
        instances.add(instance);
        return instance;
    }

    public T getInstance(Block block) {
        return getInstance(block.getLocation());
    }

    public T getInstance(Entity entity) {
        return getInstance(entity.getLocation());
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
