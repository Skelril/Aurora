/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shard.instance.Catacombs;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.skelril.OpenBoss.Boss;
import com.skelril.OpenBoss.EntityDetail;
import com.skelril.OpenBoss.SlaveBossManager;
import com.skelril.OpenBoss.instruction.processor.BindProcessor;
import com.skelril.OpenBoss.instruction.processor.DamagedProcessor;
import com.skelril.aurora.combat.bosses.instruction.NamedBindInstruction;
import com.skelril.aurora.items.custom.CustomItems;
import com.skelril.aurora.shard.ShardInstance;
import com.skelril.aurora.shard.instance.BukkitShardInstance;
import com.skelril.aurora.shard.instance.Catacombs.instruction.CatacombsHealthInstruction;
import com.skelril.aurora.shard.instance.Catacombs.instruction.bossmoves.CatacombsDamageNearby;
import com.skelril.aurora.shard.instance.Catacombs.instruction.bossmoves.UndeadMinionRetaliation;
import com.skelril.aurora.util.ChanceUtil;
import com.skelril.aurora.util.ChatUtil;
import com.skelril.aurora.util.item.ItemUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;

import static com.sk89q.commandbook.CommandBook.inst;
import static com.zachsthings.libcomponents.bukkit.BasePlugin.server;

public class CatacombsInstance extends BukkitShardInstance<CatacombsShard> implements Runnable {

    private int wave = 0;

    private Location entryPoint;

    private SlaveBossManager bosses;
    private SlaveBossManager waveMobs;

    public CatacombsInstance(CatacombsShard shard, World world, ProtectedRegion region) {
        super(shard, world, region);
        setUp();
        remove();
    }

    private void setUp() {
        Vector min = getRegion().getMinimumPoint();
        this.entryPoint = new Location(getBukkitWorld(), min.getX() + 17.5, min.getY() + 1, min.getZ() + 58.5);

        this.bosses = getMaster().getBossManager().obtainManager();
        this.waveMobs = getMaster().getWaveMobManager().obtainManager();
    }

    public static CatacombsInstance getInst(EntityDetail detail) {
        CatacombEntityDetail cDetail = CatacombEntityDetail.getFrom(detail);
        if (cDetail != null) {
            ShardInstance<?> inst = cDetail.getInstance();
            if (inst instanceof CatacombsInstance) {
                return (CatacombsInstance) inst;
            }
        }
        return null;
    }

    public int getWave() {
        return wave;
    }

    @Override
    public void teleportTo(com.sk89q.worldedit.entity.Player... players) {
        for (com.sk89q.worldedit.entity.Player player : players) {
            if (player instanceof BukkitPlayer) {
                Player bPlayer = ((BukkitPlayer) player).getPlayer();
                bPlayer.teleport(entryPoint);
            }
        }
        server().getScheduler().runTaskLater(inst(), this::checkedSpawnWave, 1);
    }

    @Override
    public void cleanUp() {
        getMaster().getBossManager().forgetManager(bosses);
        getMaster().getWaveMobManager().forgetManager(waveMobs);
        super.cleanUp();
    }
    @Override
    public void run() {
        if (isEmpty()) {
            expire();
        }
    }

    public void checkedSpawnWave() {
        if (!hasActiveMobs()) {
            remove();
            spawnWave();
        }
    }

    public boolean hasActiveMobs() {
        for (Zombie zombie : getContained(Zombie.class)) {
            if (getMaster().getWaveMobManager().lookup(zombie.getUniqueId(), waveMobs) != null) {
                return true;
            }
            if (getMaster().getBossManager().lookup(zombie.getUniqueId(), bosses) != null) {
                return true;
            }
        }
        return false;
    }

    public int getSpawnCount(int wave) {
        return (int) (Math.pow(wave, 2) + (wave * 3)) / 2;
    }

    public int getSpeed() {

        if ((wave + 1) % 5 == 0) {
            return 1;
        }

        for (Player player : getContained(Player.class)) {
            if (ItemUtil.hasItem(player, CustomItems.PHANTOM_CLOCK)) {
                return 2;
            }
        }
        return 1;
    }

    public void spawnWave() {
        wave += getSpeed();
        if (wave % 5 == 0) {
            spawnBossWave();
        } else {
            spawnNormalWave();
        }
        ChatUtil.sendError(getContained(Player.class), "Starting wave: " + wave + "!");
    }

    private void spawnBossWave() {
        Zombie z = getBukkitWorld().spawn(entryPoint, Zombie.class);
        Boss boss = new Boss(z, new CatacombEntityDetail(this, wave));

        DamagedProcessor damagedProcessor = boss.getDamagedProcessor();
        damagedProcessor.addInstruction(new CatacombsDamageNearby());
        damagedProcessor.addInstruction(new UndeadMinionRetaliation());

        getMaster().getBossManager().bind(boss, bosses);
    }

    private void spawnNormalWave() {
        com.sk89q.worldedit.Vector min = getRegion().getMinimumPoint();
        com.sk89q.worldedit.Vector max = getRegion().getMaximumPoint();

        int minX = min.getBlockX();
        int minZ = min.getBlockZ();
        int maxX = max.getBlockX();
        int maxZ = max.getBlockZ();

        final int y = min.getBlockY() + 2;
        int needed = getSpawnCount(wave);

        for (int i = needed; i > 0; --i) {
            Block block;
            do {
                int x = ChanceUtil.getRangedRandom(minX, maxX);
                int z = ChanceUtil.getRangedRandom(minZ, maxZ);

                block = getBukkitWorld().getBlockAt(x, y, z);
            } while (block.getTypeId() != 0 || block.getRelative(BlockFace.UP).getTypeId() != 0);
            spawnWaveMob(block.getLocation().add(.5, 0, .5));
        }
    }

    public void spawnWaveMob(Location loc) {
        Boss waveMob;
        if (ChanceUtil.getChance(25)) {
            waveMob = spawnStrong(loc);
        } else {
            waveMob = spawnNormal(loc);
        }

        getMaster().getWaveMobManager().bind(waveMob, waveMobs);
    }

    private Boss spawnStrong(Location loc) {
        Zombie z = loc.getWorld().spawn(loc, Zombie.class);
        Boss boss = new Boss(z, new CatacombEntityDetail(this, wave * 2));

        BindProcessor bindProcessor = boss.getBindProcessor();
        bindProcessor.addInstruction(new CatacombsHealthInstruction(25));
        bindProcessor.addInstruction(new NamedBindInstruction("Wrathful Zombie"));
        return boss;
    }

    private Boss spawnNormal(Location loc) {
        Zombie z = loc.getWorld().spawn(loc, Zombie.class);
        Boss boss = new Boss(z, new CatacombEntityDetail(this, wave));

        BindProcessor bindProcessor = boss.getBindProcessor();
        bindProcessor.addInstruction(new CatacombsHealthInstruction(20));
        bindProcessor.addInstruction(new NamedBindInstruction("Guardian Zombie"));

        return boss;
    }
}
