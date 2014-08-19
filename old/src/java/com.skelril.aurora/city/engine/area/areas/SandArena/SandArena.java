/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.city.engine.area.areas.SandArena;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.skelril.aurora.admin.AdminComponent;
import com.skelril.aurora.city.engine.area.AreaComponent;
import com.skelril.aurora.city.engine.area.PersistentArena;
import com.skelril.aurora.exceptions.UnknownPluginException;
import com.skelril.aurora.util.APIUtil;
import com.skelril.aurora.util.ChanceUtil;
import com.skelril.aurora.util.LocationUtil;
import com.skelril.aurora.util.database.IOUtil;
import com.skelril.aurora.util.player.PlayerState;
import com.zachsthings.libcomponents.InjectComponent;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;

//@ComponentInformation(friendlyName = "Sand Arena", desc = "It's a bit dry")
//@Depend(components = {AdminComponent.class}, plugins = {"WorldGuard"})
public class SandArena extends AreaComponent<SandArenaConfig> implements PersistentArena {

    @InjectComponent
    protected AdminComponent admin;

    protected int[] respawnBlocks = {BlockID.WOOD, BlockID.STONE_BRICK};
    protected HashMap<String, PlayerState> playerState = new HashMap<>();

    @Override
    public void setUp() {
        try {
            WorldGuardPlugin WG = APIUtil.getWorldGuard();
            world = server.getWorlds().get(0);
            region = WG.getRegionManager(world).getRegion("oblitus-district-arena-pvp");
            tick = 5 * 20;
            listener = new SandArenaListener(this);
            config = new SandArenaConfig();
            reloadData();
        } catch (UnknownPluginException e) {
            log.info("WorldGuard could not be found!");
        }
    }

    @Override
    public void enable() {
        // WorldGuard loads late for some reason
        server.getScheduler().runTaskLater(inst, super::enable, 1);
    }

    @Override
    public void disable() {
        writeData(false);
    }

    @Override
    public void run() {
        if (!isEmpty()) {
            addBlocks();
            equalize();
        }
        removeBlocks();
        writeData(true);
    }

    private void equalize() {
        for (Player player : getContained(Player.class)) {
            try {
                admin.deadmin(player);
            } catch (Exception e) {
                log.warning("The player: " + player.getName() + " may have an unfair advantage.");
            }
        }
    }

    public void addBlocks() {
        CuboidRegion dropArea = new CuboidRegion(region.getMaximumPoint(), region.getMinimumPoint());

        if (dropArea.getArea() > 75000) {
            log.warning("The region: " + getRegion().getId() + " is too large.");
            return;
        }

        com.sk89q.worldedit.Vector min = getRegion().getMinimumPoint();
        com.sk89q.worldedit.Vector max = getRegion().getMaximumPoint();

        int minX = min.getBlockX();
        int minZ = min.getBlockZ();
        int minY = min.getBlockY();
        int maxX = max.getBlockX();
        int maxZ = max.getBlockZ();
        int maxY = max.getBlockY();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = maxY; y >= minY; --y) {
                    Block block = getWorld().getBlockAt(x, y, z);
                    Block topBlock = getWorld().getBlockAt(x, y + 1, z);

                    if (y == minY) {
                        block.setTypeIdAndData(BlockID.SAND, (byte) 0, false);
                    }

                    if (!(y + 1 > getWorld().getMaxHeight())
                            && !(y + 1 > maxY)
                            && block.getTypeId() != BlockID.AIR
                            && topBlock.getTypeId() == BlockID.AIR
                            && !LocationUtil.isCloseToPlayer(block, 4)) {
                        if (ChanceUtil.getChance(config.increaseRate)) {
                            topBlock.setTypeIdAndData(BlockID.SAND, (byte) 0, false);
                        }
                        break;
                    }
                }
            }
        }
    }

    public void removeBlocks() {
        com.sk89q.worldedit.Vector min = region.getMinimumPoint();
        com.sk89q.worldedit.Vector max = region.getMaximumPoint();

        int minX = min.getBlockX();
        int minY = min.getBlockY();
        int minZ = min.getBlockZ();
        int maxX = max.getBlockX();
        int maxZ = max.getBlockZ();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                int sizeY = world.getHighestBlockYAt(x, z) - minY;

                for (int y = sizeY; y > 0; y--) {
                    Block block = world.getBlockAt(x, y + minY, z);

                    if (!block.getChunk().isLoaded()) break;
                    if (!cachedEmpty()) {
                        if (y + minY < world.getMaxHeight()
                                && ChanceUtil.getChance(config.decreaseRate - (y * ChanceUtil.getRandom(5)))
                                && !LocationUtil.isCloseToPlayer(block, 4)) {
                            block.setTypeIdAndData(BlockID.AIR, (byte) 0, false);
                        } else {
                            break;
                        }
                    } else {
                        if (y + minY < world.getMaxHeight()
                                && ChanceUtil.getChance((config.decreaseRate - (y * ChanceUtil.getRandom(5))) / 4)) {
                            block.setTypeIdAndData(BlockID.AIR, (byte) 0, false);
                        } else {
                            break;
                        }
                    }
                }
            }
        }
    }

    public Location getRespawnLocation() {
        Vector v;
        Vector min = getRegion().getParent().getMinimumPoint();
        Vector max = getRegion().getParent().getMaximumPoint();

        do {
            v = LocationUtil.pickLocation(min.getX(), max.getX(), min.getY(), max.getY(), min.getZ(), max.getZ());
        } while (getRegion().contains(v) || !isRespawnBlock(v) || getBlock(v).getTypeId() != 0);
        return new Location(world, v.getX(), v.getY(), v.getZ());
    }

    private boolean isRespawnBlock(Vector v) {
        for (int block : respawnBlocks) {
            if (block == getBlock(v.add(0, -1, 0)).getTypeId()) return true;
        }
        return false;
    }

    private Block getBlock(Vector v) {
        return getWorld().getBlockAt(v.getBlockX(), v.getBlockY(), v.getBlockZ());
    }

    @Override
    public void writeData(boolean doAsync) {
        Runnable run = () -> {
            respawnsFile:
            {
                File playerStateFile = new File(getWorkingDir().getPath() + "/respawns.dat");
                if (playerStateFile.exists()) {
                    Object playerStateFileO = IOUtil.readBinaryFile(playerStateFile);

                    if (playerState.equals(playerStateFileO)) {
                        break respawnsFile;
                    }
                }
                IOUtil.toBinaryFile(getWorkingDir(), "respawns", playerState);
            }
        };
        if (doAsync) {
            server.getScheduler().runTaskAsynchronously(inst, run);
        } else {
            run.run();
        }
    }

    @Override
    public void reloadData() {
        File playerStateFile = new File(getWorkingDir().getPath() + "/respawns.dat");
        if (playerStateFile.exists()) {
            Object playerStateFileO = IOUtil.readBinaryFile(playerStateFile);
            if (playerStateFileO instanceof HashMap) {
                //noinspection unchecked
                playerState = (HashMap<String, PlayerState>) playerStateFileO;
                log.info("Loaded: " + playerState.size() + " respawn records for the Sand Arena.");
            } else {
                log.warning("Invalid block record file encountered: " + playerStateFile.getName() + "!");
                log.warning("Attempting to use backup file...");
                playerStateFile = new File(getWorkingDir().getPath() + "/old-" + playerStateFile.getName());
                if (playerStateFile.exists()) {
                    playerStateFileO = IOUtil.readBinaryFile(playerStateFile);
                    if (playerStateFileO instanceof HashMap) {
                        //noinspection unchecked
                        playerState = (HashMap<String, PlayerState>) playerStateFileO;
                        log.info("Backup file loaded successfully!");
                        log.info("Loaded: " + playerState.size() + " respawn records for the Sand Arena.");
                    } else {
                        log.warning("Backup file failed to load!");
                    }
                }
            }
        }
    }
}
