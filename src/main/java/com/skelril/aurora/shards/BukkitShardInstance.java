/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shards;

import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.skelril.aurora.shard.Shard;
import com.skelril.aurora.shard.ShardInstance;
import com.skelril.aurora.util.LocationUtil;
import com.skelril.aurora.util.WEAPIUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Monster;

import java.util.Collection;
import java.util.stream.Collectors;

import static com.zachsthings.libcomponents.bukkit.BasePlugin.server;

public abstract class BukkitShardInstance<S extends Shard> extends ShardInstance<S> {

    private boolean expired = false;
    private boolean empty = true;

    public BukkitShardInstance(S shard, World world, ProtectedRegion region) {
        super(shard, world, region);
    }

    @Override
    public boolean isActive() {
        return !expired;
    }

    public void expire() {
        expired = true;
        teleportAll(Bukkit.getWorlds().get(0).getSpawnLocation());
    }

    @Override
    public void cleanUp() {
        remove();
    }

    public void teleportAll(Location location) {
        for (org.bukkit.entity.Player player : getContained(org.bukkit.entity.Player.class)) {
            player.teleport(location);
        }
    }

    public <K extends Entity> Collection<K> getContained(Class<K> clazz) {
        return getContained(0, clazz);
    }

    public <K extends Entity> Collection<K> getContained(int parentsUp, Class<K> clazz) {
        ProtectedRegion r = region;
        for (int i = parentsUp; i > 0; i--) r = r.getParent();
        return getContained(r, clazz);
    }

    public <K extends Entity> Collection<K> getContained(ProtectedRegion region, Class<K> clazz) {
        return getBukkitWorld().getEntitiesByClass(clazz).stream()
                .filter(e -> e.isValid() && LocationUtil.isInRegion(region, e))
                .collect(Collectors.toList());
    }

    public Collection<Entity> getContained() {
        return getContained(Entity.class);
    }

    public Collection<Entity> getContained(int parentsUp) {
        return getContained(parentsUp, Entity.class);
    }

    public Collection<Entity> getContained(Class<?>... classes) {
        return getContained(0, classes);
    }

    public Collection<Entity> getContained(int parentsUp, Class<?>... classes) {
        ProtectedRegion r = region;
        for (int i = parentsUp; i > 0; i--) r = r.getParent();
        return getContained(r, classes);
    }

    public Collection<Entity> getContained(ProtectedRegion region, Class<?>... classes) {
        return getBukkitWorld().getEntitiesByClasses(classes).stream()
                .filter(e -> e.isValid() && LocationUtil.isInRegion(region, e))
                .collect(Collectors.toList());
    }

    public boolean isEmpty() {
        for (org.bukkit.entity.Player player : server().getOnlinePlayers()) {
            if (contains(player)) {
                empty = false;
                return false;
            }
        }
        empty = true;
        return true;
    }

    public boolean cachedEmpty() {
        return empty;
    }

    public boolean contains(Entity entity) {
        return contains(entity.getLocation());
    }

    public boolean contains(Entity entity, int parentsUp) {
        return contains(entity.getLocation(), parentsUp);
    }

    public boolean contains(ProtectedRegion region, Entity entity) {
        return contains(region, entity.getLocation());
    }

    public boolean contains(Block block) {
        return contains(block.getLocation());
    }

    public boolean contains(Block block, int parentsUp) {
        return contains(block.getLocation(), parentsUp);
    }

    public boolean contains(ProtectedRegion region, Block block) {
        return contains(region, block.getLocation());
    }

    public boolean contains(Location location) {
        return contains(region, location);
    }

    public boolean contains(Location location, int parentsUp) {
        ProtectedRegion r = region;
        for (int i = parentsUp; i > 0; i--) r = r.getParent();
        return contains(r, location);
    }

    public boolean contains(ProtectedRegion region, Location location) {
        return LocationUtil.isInRegion(getBukkitWorld(), region, location);
    }

    public void remove() {
        remove(Monster.class, ExperienceOrb.class, Arrow.class);
    }

    public void remove(Class<?>... classes) {
        getContained(classes).stream()
                .filter(e -> !(e instanceof org.bukkit.entity.Player))
                .forEach(Entity::remove);
    }

    public org.bukkit.World getBukkitWorld() {
        return WEAPIUtil.getWorld(world);
    }
}
