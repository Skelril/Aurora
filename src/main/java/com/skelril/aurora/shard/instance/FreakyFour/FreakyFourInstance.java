/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shard.instance.FreakyFour;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.skelril.OpenBoss.Boss;
import com.skelril.OpenBoss.BossManager;
import com.skelril.OpenBoss.EntityDetail;
import com.skelril.aurora.combat.bosses.detail.SBossDetail;
import com.skelril.aurora.shard.ShardInstance;
import com.skelril.aurora.shard.instance.BukkitShardInstance;
import com.skelril.aurora.shard.instance.FreakyFour.boss.CharlotteBossManager;
import com.skelril.aurora.util.ChanceUtil;
import com.skelril.aurora.util.EnvironmentUtil;
import com.skelril.aurora.util.LocationUtil;
import com.skelril.aurora.util.checker.Expression;
import com.skelril.aurora.util.item.ItemUtil;
import com.skelril.aurora.util.timer.IntegratedRunnable;
import com.skelril.aurora.util.timer.TimedRunnable;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

import static com.sk89q.commandbook.CommandBook.inst;
import static com.sk89q.commandbook.util.entity.EntityUtil.sendProjectilesFromEntity;
import static com.zachsthings.libcomponents.bukkit.BasePlugin.callEvent;
import static com.zachsthings.libcomponents.bukkit.BasePlugin.server;

public class FreakyFourInstance extends BukkitShardInstance<FreakyFourShard> implements Runnable {

    private FreakyFourBoss currentboss = FreakyFourBoss.CHARLOTTE;

    private EnumMap<FreakyFourBoss, Boss> bosses = new EnumMap<>(FreakyFourBoss.class);
    private EnumMap<FreakyFourBoss, CuboidRegion> regions = new EnumMap<>(FreakyFourBoss.class);

    private List<Boss> charlotteMinions = new ArrayList<>();
    private int tick = 0;

    public FreakyFourInstance(FreakyFourShard shard, World world, ProtectedRegion region) {
        super(shard, world, region);
        setUp();
        remove();
        spawnBoss(currentboss);
    }

    private void setUp() {
        Vector offset = region.getMinimumPoint();
        regions.put(FreakyFourBoss.CHARLOTTE, new CuboidRegion(offset.add(72, 7, 1), offset.add(94, 12, 42)));
        regions.put(FreakyFourBoss.FRIMUS, new CuboidRegion(offset.add(48, 7, 1), offset.add(70, 12, 42)));
        regions.put(FreakyFourBoss.DA_BOMB, new CuboidRegion(offset.add(24, 7, 1), offset.add(46, 12, 42)));
        regions.put(FreakyFourBoss.SNIPEE, new CuboidRegion(offset.add(1, 7, 1), offset.add(22, 12, 42)));
    }

    public static FreakyFourInstance getInst(EntityDetail detail) {
        if (detail instanceof SBossDetail) {
            ShardInstance<?> inst = ((SBossDetail) detail).getInstance();
            if (inst instanceof FreakyFourInstance) {
                return (FreakyFourInstance) inst;
            }
        }
        return null;
    }

    @Override
    public void teleportTo(com.sk89q.worldedit.entity.Player... players) {
        Location target = getCenter(currentboss);
        for (com.sk89q.worldedit.entity.Player player : players) {
            if (player instanceof BukkitPlayer) {
                Player bPlayer = ((BukkitPlayer) player).getPlayer();
                bPlayer.teleport(target);
            }
        }
    }

    @Override
    public void run() {
        if (isEmpty()) {
            expire();
            return;
        }
        equalize();
        ++tick;
        if (tick % 6 == 0) {
            fakeXPGain();
            for (FreakyFourBoss boss : FreakyFourBoss.values()) {
                if (isSpawned(boss)) {
                    run(boss);
                }
            }
        }
    }

    public void equalize() {
        for (Player player : getContained(Player.class)) {
            getMaster().getAdmin().deadmin(player);
        }
    }

    public void fakeXPGain() {
        for (Player player : getContained(Player.class)) {
            if (!ItemUtil.hasNecrosArmor(player)) continue;
            for (int i = ChanceUtil.getRandom(5); i > 0; --i) {
                callEvent(
                        new PlayerExpChangeEvent(
                                player,
                                ChanceUtil.getRandom(getMaster().getConfig().fakeXP)
                        )
                );
            }
        }
    }

    @Override
    public void prepare() {
        for (FreakyFourBoss boss : FreakyFourBoss.values()) {
            prepare(boss);
        }
        super.prepare();
    }

    public void prepare(FreakyFourBoss boss) {
        switch (boss) {
            case CHARLOTTE:
                prepareCharlotte();
                break;
            case FRIMUS:
                prepareFrimus();
                break;
        }
    }

    private void prepareCharlotte() {
        CuboidRegion charlotte_RG = regions.get(FreakyFourBoss.CHARLOTTE);
        final Vector min = charlotte_RG.getMinimumPoint();
        final Vector max = charlotte_RG.getMaximumPoint();
        int minX = min.getBlockX();
        int minY = min.getBlockY();
        int minZ = min.getBlockZ();
        int maxX = max.getBlockX();
        int maxY = max.getBlockY();
        int maxZ = max.getBlockZ();

        for (int y = minY; y <= maxY; ++y) {
            for (int x = minX; x <= maxX; ++x) {
                for (int z = minZ; z <= maxZ; ++z) {
                    Block block = getBukkitWorld().getBlockAt(x, y, z);
                    if (block.getType() == Material.WEB) {
                        block.setType(Material.AIR);
                    }
                }
            }
        }

        for (Entity spider : getContained(charlotte_RG, Spider.class, CaveSpider.class)) {
            spider.remove();
        }
    }

    private void prepareFrimus() {
        CuboidRegion frimus_RG = regions.get(FreakyFourBoss.FRIMUS);
        final Vector min = frimus_RG.getMinimumPoint();
        final Vector max = frimus_RG.getMaximumPoint();
        int minX = min.getBlockX();
        int minY = min.getBlockY();
        int minZ = min.getBlockZ();
        int maxX = max.getBlockX();
        int maxY = max.getBlockY();
        int maxZ = max.getBlockZ();

        for (int y = minY; y <= maxY; ++y) {
            for (int x = minX; x <= maxX; ++x) {
                for (int z = minZ; z <= maxZ; ++z) {
                    Block block = getBukkitWorld().getBlockAt(x, y, z);
                    if (block.getType() == Material.FIRE || EnvironmentUtil.isLava(block.getTypeId())) {
                        block.setType(Material.AIR);
                    }
                }
            }
        }
    }

    @Override
    public void cleanUp() {
        for (FreakyFourBoss boss : FreakyFourBoss.values()) {
            cleanUp(boss);
        }
        cleanUpCharlotteMinions();
        super.cleanUp();
    }

    public void cleanUp(FreakyFourBoss boss) {
        Boss aBoss = bosses.get(boss);
        if (aBoss != null) {
            getMaster().getManager(boss).silentUnbind(aBoss);
        }
    }

    public void cleanUpCharlotteMinions() {
        BossManager manager = getMaster().getManager(FreakyFourBoss.CHARLOTTE);
        if (manager instanceof CharlotteBossManager) {
            CharlotteBossManager bossManager = (CharlotteBossManager) manager;
            charlotteMinions.forEach(bossManager::silentBind);
        }
    }

    public boolean isSpawned(FreakyFourBoss boss) {
        getContained(getRegion(boss), boss.getEntityClass()).stream().filter(Entity::isValid).forEach(e -> {
            Boss b = getMaster().getManager(boss).updateLookup(e);
            if (b == null && !(e instanceof CaveSpider)) {
                e.remove();
            }
        });
        return bosses.get(boss) != null;
    }

    public LivingEntity getBoss(FreakyFourBoss boss) {
        Boss aBoss = bosses.get(boss);
        return aBoss != null ? aBoss.getEntity() : null;
    }

    public FreakyFourBoss getCurrentboss() {
        return currentboss;
    }

    public void setCurrentboss(FreakyFourBoss currentBoss) {
        this.currentboss = currentBoss;
    }

    public Location getCenter(FreakyFourBoss boss) {
        return LocationUtil.getCenter(getBukkitWorld(), regions.get(boss));
    }

    public CuboidRegion getRegion(FreakyFourBoss boss) {
        return regions.get(boss);
    }

    public void spawnBoss(FreakyFourBoss boss) {
        spawnBoss(boss, 5 * 20);
    }

    public void spawnBoss(FreakyFourBoss boss, long delay) {
        server().getScheduler().runTaskLater(inst(), () -> {
            LivingEntity entity = getBukkitWorld().spawn(getCenter(boss), boss.getEntityClass());
            Boss aBoss = new Boss(entity, new SBossDetail(this));
            getMaster().getManager(boss).bind(aBoss);
            bosses.put(boss, aBoss);
        }, delay);
    }

    public void bossDied(FreakyFourBoss boss) {
        bosses.put(boss, null);
    }

    public void run(FreakyFourBoss boss) {
        switch (boss) {
            case CHARLOTTE:
                runCharlotte();
                break;
            case FRIMUS:
                runFrimus();
                break;
            case SNIPEE:
                runSnipee();
                break;
        }
    }

    private void createWall(CuboidRegion region,
                            Expression<Block, Boolean> oldExpr,
                            Expression<Block, Boolean> newExpr,
                            Material oldType, Material newType,
                            int density, int floodFloor) {

        final Vector min = region.getMinimumPoint();
        final Vector max = region.getMaximumPoint();
        int minX = min.getBlockX();
        int minY = min.getBlockY();
        int minZ = min.getBlockZ();
        int maxX = max.getBlockX();
        int maxY = max.getBlockY();
        int maxZ = max.getBlockZ();

        int initialTimes = maxZ - minZ + 1;
        IntegratedRunnable integratedRunnable = new IntegratedRunnable() {
            @Override
            public boolean run(int times) {
                int startZ = minZ + (initialTimes - times) - 1;

                for (int x = minX; x <= maxX; ++x) {
                    for (int z = startZ; z < Math.min(maxZ, startZ + 4); ++z) {
                        boolean flood = ChanceUtil.getChance(density);
                        for (int y = minY; y <= maxY; ++y) {
                            Block block = getBukkitWorld().getBlockAt(x, y, z);
                            if (z == startZ && newExpr.evaluate(block)) {
                                block.setType(oldType);
                            } else if (flood && oldExpr.evaluate(block)) {
                                block.setType(newType);
                            }
                        }
                    }
                }
                return true;
            }

            @Override
            public void end() {
                if (floodFloor != -1) {
                    for (int x = minX; x <= maxX; ++x) {
                        for (int z = minZ; z <= maxZ; ++z) {
                            if (!ChanceUtil.getChance(floodFloor)) continue;
                            Block block = getBukkitWorld().getBlockAt(x, minY, z);
                            if (oldExpr.evaluate(block)) {
                                block.setType(newType);
                            }
                        }
                    }
                }
            }
        };
        TimedRunnable timedRunnable = new TimedRunnable(integratedRunnable, initialTimes);
        BukkitTask task = server().getScheduler().runTaskTimer(inst(), timedRunnable, 0, 5);
        timedRunnable.setTask(task);
    }

    private void runCharlotte() {
        LivingEntity charlotte = getBoss(FreakyFourBoss.CHARLOTTE);
        for (int i = ChanceUtil.getRandom(10); i > 0; --i) {
            spawnCharlotteMinion(charlotte.getLocation());
        }

        CuboidRegion charlotte_RG = regions.get(FreakyFourBoss.CHARLOTTE);
        final Vector min = charlotte_RG.getMinimumPoint();
        final Vector max = charlotte_RG.getMaximumPoint();
        int minX = min.getBlockX();
        int minY = min.getBlockY();
        int minZ = min.getBlockZ();
        int maxX = max.getBlockX();
        int maxY = max.getBlockY();
        int maxZ = max.getBlockZ();

        switch (ChanceUtil.getRandom(3)) {
            case 1:
                createWall(
                        charlotte_RG,
                        input -> input.getType() == Material.AIR,
                        input -> input.getType() == Material.WEB,
                        Material.AIR,
                        Material.WEB,
                        1,
                        getMaster().getConfig().charlotteFloorWeb
                );
                break;
            case 2:
                if (charlotte instanceof Monster) {
                    LivingEntity target = ((Monster) charlotte).getTarget();
                    if (target != null && contains(target)) {
                        List<Location> queList = new ArrayList<>();
                        for (Location loc : Arrays.asList(target.getLocation(), target.getEyeLocation())) {
                            for (BlockFace face : EnvironmentUtil.getNearbyBlockFaces()) {
                                if (face == BlockFace.SELF) continue;
                                queList.add(loc.getBlock().getRelative(face).getLocation());
                            }
                        }
                        for (Location loc : queList) {
                            Block block = getBukkitWorld().getBlockAt(loc);
                            if (block.getType().isSolid()) continue;
                            block.setType(Material.WEB);
                        }
                    }
                }
                break;
            case 3:
                for (int y = minY; y <= maxY; ++y) {
                    for (int x = minX; x <= maxX; ++x) {
                        for (int z = minZ; z <= maxZ; ++z) {
                            if (!ChanceUtil.getChance(getMaster().getConfig().charlotteWebSpider)) continue;
                            Block block = getBukkitWorld().getBlockAt(x, y, z);
                            if (block.getType() == Material.WEB) {
                                block.setType(Material.AIR);
                                spawnCharlotteMinion(block.getLocation());
                            }
                        }
                    }
                }
                break;
        }
    }

    private void spawnCharlotteMinion(Location location) {
        LivingEntity entity = location.getWorld().spawn(location, CaveSpider.class);
        BossManager manager = getMaster().getManager(FreakyFourBoss.CHARLOTTE);
        if (manager instanceof CharlotteBossManager) {
            Boss boss = new Boss(entity, new SBossDetail(this));
            charlotteMinions.add(boss);
            ((CharlotteBossManager) manager).getMinionManager().bind(boss);
        }
    }

    private void runFrimus() {
        createWall(
                getRegion(FreakyFourBoss.FRIMUS),
                input -> input.getType() == Material.AIR,
                EnvironmentUtil::isLava,
                Material.AIR,
                Material.LAVA,
                getMaster().getConfig().frimusWallDensity,
                -1
        );
    }

    private void runSnipee() {
        sendProjectilesFromEntity(getBoss(FreakyFourBoss.SNIPEE), 20, 1.6F, Arrow.class);
    }
}
