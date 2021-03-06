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

package com.skelril.aurora.util;

import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.skelril.aurora.util.checker.RegionChecker;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class LocationUtil {

    private static final BlockFace[] surroundingBlockFaces = new BlockFace[]{
            BlockFace.NORTH, BlockFace.EAST,
            BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH_EAST, BlockFace.NORTH_WEST, BlockFace.SOUTH_EAST,
            BlockFace.SOUTH_WEST
    };

    public static double distanceSquared2D(Location a, Location b) {

        return Math.pow(a.getX() - b.getX(), 2) + Math.pow(a.getZ() - b.getZ(), 2);
    }

    public static Location grandBank(World world) {

        if (!world.getName().equals("City")) return null;
        return new Location(world, 592, 83, 1176.5);
    }

    public static boolean toGround(Player player) {
        Location target = findFreePosition(player.getLocation());
        return target != null && player.teleport(target);
    }

    public static Location findFreePosition(final Location pos) {
        return findFreePosition(pos, true);
    }

    public static Location findFreePosition(final Location pos, boolean center) {

        // Find the raw safe position
        Location mPos = findRawFreePosition(pos);

        // Null safety check
        if (mPos == null) {
            return null;
        }

        // Add the offset from the original position
        mPos.add(pos.getX() - pos.getBlockX(), 0, pos.getZ() - pos.getBlockZ());

        // Move to the center of the block if undefined
        if (center) {
            if (mPos.getX() == mPos.getBlockX()) mPos.add(.5, 0, 0);
            if (mPos.getZ() == mPos.getBlockZ()) mPos.add(0, 0, .5);
        }
        return mPos;
    }

    public static Location findRawFreePosition(final Location pos) {

        World world = pos.getWorld();

        // Let's try going down
        Block block = pos.getBlock().getRelative(0, 1, 0);
        if (!block.getChunk().isLoaded()) {
            block.getChunk().load();
        }
        int free = 0;

        // Look for ground
        while (block.getY() > 1 && (BlockType.canPassThrough(block.getTypeId()) || block.getTypeId() == BlockID.BED)) {
            free++;
            block = block.getRelative(0, -1, 0);
        }

        if (block.getY() == 0) return null; // No ground below!

        if (free >= 2) {
            if (block.getTypeId() == BlockID.LAVA || block.getTypeId() == BlockID.STATIONARY_LAVA) {
                return null; // Not safe
            }

            Block tb = block.getRelative(0, 1, 0);
            Location l = tb.getLocation();
            l.add(new Vector(0, BlockType.centralTopLimit(tb.getTypeId(), tb.getData()), 0));
            l.setPitch(pos.getPitch());
            l.setYaw(pos.getYaw());
            return l;
        }

        // Let's try going up
        block = pos.getBlock().getRelative(0, -1, 0);
        free = 0;
        boolean foundGround = false;

        while (block.getY() + 1 < world.getMaxHeight()) {
            if (BlockType.canPassThrough(block.getTypeId()) || block.getTypeId() == BlockID.BED) {
                free++;
            } else {
                free = 0;
                foundGround = block.getTypeId() != BlockID.LAVA && block.getTypeId() != BlockID.STATIONARY_LAVA;
            }

            if (foundGround && free == 2) {
                Block tb = block.getRelative(0, -1, 0);
                Location l = tb.getLocation();
                l.add(new Vector(0, BlockType.centralTopLimit(tb.getTypeId(), tb.getData()), 0));
                l.setPitch(pos.getPitch());
                l.setYaw(pos.getYaw());
                return l;
            }

            block = block.getRelative(0, 1, 0);
        }

        return null;
    }

    public static Location findRandomLoc(Location searchFromLocation, final int radius) {

        return findRandomLoc(searchFromLocation.getBlock(), radius);
    }

    public static Location findRandomLoc(Block searchFromLocation, final int radius) {

        return findRandomLoc(searchFromLocation, radius, false);
    }

    public static Location findRandomLoc(Location searchFromLocation, final int radius,
                                         boolean trueRandom) {

        return findRandomLoc(searchFromLocation.getBlock(), radius, trueRandom);
    }

    public static Location findRandomLoc(Location searchFromLocation, final int radius,
                                         boolean trueRandom, boolean airOnly) {

        return findRandomLoc(searchFromLocation.getBlock(), radius, trueRandom, airOnly);
    }

    public static Location findRandomLoc(Block searchFromLocation, final int radius, boolean trueRandom) {

        return findRandomLoc(searchFromLocation, radius, trueRandom, true);
    }

    public static Location findRandomLoc(Block searchFromLocation, final int radius, boolean trueRandom, boolean airOnly) {

        int trueRadius = trueRandom ? ChanceUtil.getRandom(radius) : radius;

        BlockFace dir;
        do {
            dir = CollectionUtil.getElement(BlockFace.values());
        } while (dir == null);

        if (airOnly) {
            return findFreePosition(searchFromLocation.getRelative(dir, trueRadius).getLocation());
        } else {
            return searchFromLocation.getRelative(dir, trueRadius).getLocation();
        }
    }

    public static boolean containsPlayer(World world, ProtectedRegion region) {

        for (Player player : world.getPlayers()) {
            if (isInRegion(region, player)) return true;
        }
        return false;
    }

    public static boolean containsPlayer(World world, Region region) {
        for (Player player : world.getPlayers()) {
            if (isInRegion(region, player)) return true;
        }
        return false;
    }

    public static boolean isInRegion(Region region, Entity entity) {
        return isInRegion(region, entity.getLocation());
    }

    public static boolean isInRegion(Region region, Location location) {
        return region.contains(new com.sk89q.worldedit.Vector(
                location.getX(),
                location.getY(),
                location.getZ()
        ));
    }

    public static boolean isInRegion(ProtectedRegion region, Entity entity) {

        return isInRegion(region, entity.getLocation());
    }

    public static boolean isInRegion(ProtectedRegion region, Location loc) {

        return region.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

    }

    public static boolean isInRegion(World world, ProtectedRegion region, Entity entity) {

        return isInRegion(world, region, entity.getLocation());
    }

    public static boolean isInRegion(World world, ProtectedRegion region, Location loc) {

        return world.equals(loc.getWorld()) && region.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

    }

    public static List<Player> getPlayersStandingOnRegion(World world, ProtectedRegion region) {

        List<Player> playerList = new ArrayList<>();
        for (Player player : world.getPlayers()) {
            Block block = player.getLocation().getBlock();
            if (region.contains(block.getX(), block.getY() - 1, block.getZ())
                    || region.contains(block.getX(), block.getY() - 2, block.getZ())) {
                playerList.add(player);
            }
        }
        return playerList;
    }

    public static boolean isBelowPlayer(World world, ProtectedRegion region) {

        for (Player player : world.getPlayers()) {
            Block block = player.getLocation().getBlock();
            if (region.contains(block.getX(), block.getY() - 1, block.getZ())
                    || region.contains(block.getX(), block.getY() - 2, block.getZ())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isBelowPlayer(World world, ProtectedRegion region, Player player) {

        Block block = player.getLocation().getBlock();
        return (region.contains(block.getX(), block.getY() - 1, block.getZ())
                || region.contains(block.getX(), block.getY() - 2, block.getZ()))
                && world.equals(player.getWorld());
    }

    public static boolean isCloseToPlayer(Block block, int distance) {

        return isCloseToPlayer(block.getLocation(), distance);
    }

    public static boolean isCloseToPlayer(Location location, int distance) {

        int DISTANCE_SQ = distance * distance;

        for (Player player : location.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(location) <= DISTANCE_SQ) {
                return true;
            }
        }
        return false;
    }

    public static Location[] getNearbyLocations(Location startLocation, int searchRadius) {

        return getNearbyLocations(startLocation, searchRadius, 0);
    }

    public static Location[] getNearbyLocations(Location startLocation, int searchRadius, int vert) {

        if (searchRadius < 1) {
            return new Location[]{startLocation};
        }

        int length = BlockFace.values().length - 2;

        Location[] locations;

        if (vert > 0) {
            locations = new Location[(searchRadius * length) + (searchRadius * length * (vert * 2))];
        } else {
            locations = new Location[searchRadius * length];
        }

        int locationNumber = 0;

        for (int r = 0; r < searchRadius; r++) {
            for (BlockFace blockFace : BlockFace.values()) {
                if (blockFace.getModY() != 0) continue;
                locations[locationNumber] = startLocation.getBlock().getRelative(blockFace, r).getLocation();
                locationNumber++;
            }
        }

        if (vert > 0) {
            for (int v = 0; v < vert; v++) {
                for (int r = 0; r < searchRadius; r++) {
                    for (BlockFace blockFace : BlockFace.values()) {
                        locations[locationNumber] = startLocation.getBlock().getRelative(blockFace,
                                r).getRelative(BlockFace.UP, v + 1).getLocation();
                        locationNumber++;
                    }

                    for (BlockFace blockFace : BlockFace.values()) {
                        locations[locationNumber] = startLocation.getBlock().getRelative(blockFace,
                                r).getRelative(BlockFace.DOWN, v + 1).getLocation();
                        locationNumber++;
                    }
                }
            }
        }

        return locations;
    }

    public static Location pickLocation(World world, double y, RegionChecker checker) {
        com.sk89q.worldedit.Vector max = checker.get().getMaximumPoint();
        com.sk89q.worldedit.Vector min = checker.get().getMinimumPoint();

        com.sk89q.worldedit.Vector v;
        do {
            v = LocationUtil.pickLocation(min.getX(), max.getX(), min.getZ(), max.getZ()).setY(y);
        } while (!checker.evaluate(v));

        return new Location(world, v.getX(), y, v.getZ());
    }

    public static com.sk89q.worldedit.Vector pickLocation(com.sk89q.worldedit.Vector min,
                                                          com.sk89q.worldedit.Vector max) {
        return pickLocation(min.getX(), max.getX(), min.getZ(), max.getZ());
    }

    public static com.sk89q.worldedit.Vector pickLocation(double minX, double maxX,
                                                          double minZ, double maxZ) {

        double x;
        double z;

        if (minX > maxX) {
            x = ChanceUtil.getRangedRandom(maxX, minX);
        } else {
            x = ChanceUtil.getRangedRandom(minX, maxX);
        }

        if (minZ > maxZ) {
            z = ChanceUtil.getRangedRandom(maxZ, minZ);
        } else {
            z = ChanceUtil.getRangedRandom(minZ, maxZ);
        }

        return new com.sk89q.worldedit.Vector(x, 0, z);
    }

    public static com.sk89q.worldedit.Vector pickLocation(double minX, double maxX,
                                                          double minY, double maxY,
                                                          double minZ, double maxZ) {

        double y;

        if (minY > maxY) {
            y = ChanceUtil.getRangedRandom(maxY, minY);
        } else {
            y = ChanceUtil.getRangedRandom(minY, maxY);
        }

        return pickLocation(minX, maxX, minZ, maxZ).add(0, y, 0);
    }

    public static boolean isLocNearby(Location startLocation, Location location, int searchRadius) {

        for (Location checkLocation : getNearbyLocations(startLocation, searchRadius)) {
            if (location.equals(checkLocation)) {
                return true;
            }
        }
        return false;
    }

    public static boolean getBelowID(Location loc, int id) {

        Block searchBlock = loc.add(0, -1, 0).getBlock();
        if (searchBlock.getTypeId() == id) return true;
        if (searchBlock.getTypeId() != BlockID.AIR) return false;

        for (BlockFace blockFace : surroundingBlockFaces) {
            if (searchBlock.getRelative(blockFace).getTypeId() == id) return true;
        }
        return false;
    }

    public static Location getCenter(World world, ProtectedRegion region) {
        CuboidRegion cRegion = new CuboidRegion(region.getMinimumPoint(), region.getMaximumPoint());
        com.sk89q.worldedit.Vector center = cRegion.getCenter();
        return new Location(world, center.getBlockX() + .5, center.getBlockY(), center.getBlockZ() + .5);
    }

    public static Location getCenter(World world, Region region) {
        com.sk89q.worldedit.Vector center = region.getCenter();
        return new Location(world, center.getBlockX() + .5, center.getBlockY(), center.getBlockZ() + .5);
    }
}
