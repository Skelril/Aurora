/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shards.PatientX;

import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.skelril.OpenBoss.Boss;
import com.skelril.OpenBoss.EntityDetail;
import com.skelril.aurora.admin.AdminComponent;
import com.skelril.aurora.bosses.detail.SBossDetail;
import com.skelril.aurora.shard.ShardInstance;
import com.skelril.aurora.shards.BukkitShardInstance;
import com.skelril.aurora.util.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PatientXInstance extends BukkitShardInstance<PatientXShard> implements Runnable {

    private CuboidRegion ice, drops, entry;

    private Boss boss = null;
    private long attackDur = 0;
    private int lastAttack = 0;
    private long lastDeath = 0;
    private long lastTelep = 0;
    private int emptyTicks = 0;
    private double difficulty;

    private List<Location> destinations = new ArrayList<>();

    public PatientXInstance(PatientXShard shard, World world, ProtectedRegion region) {
        super(shard, world, region);
        setUp();
        removeMobs();
        spawnBoss();
    }

    public void setUp() {

    }

    public void setDifficulty(double difficulty) {
        PatientXConfig config = getMaster().getConfig();
        this.difficulty = Math.max(config.minDifficulty, Math.min(config.maxDifficulty, difficulty));
    }

    public void resetDifficulty() {
        setDifficulty(getMaster().getConfig().defaultDifficulty);
    }

    public void modifyDifficulty(double amt) {
        setDifficulty(this.difficulty + amt);
    }

    protected Location getRandomDest() {
        return CollectionUtil.getElement(destinations);
    }

    public Location getCentralLoc() {
        return LocationUtil.getCenter(getBukkitWorld(), getRegion());
    }

    public static PatientXInstance getInst(EntityDetail detail) {
        if (detail instanceof SBossDetail) {
            ShardInstance<?> inst = ((SBossDetail) detail).getInstance();
            if (inst instanceof PatientXInstance) {
                return (PatientXInstance) inst;
            }
        }
        return null;
    }

    @Override
    public void teleportTo(com.sk89q.worldedit.entity.Player... players) {
        for (com.sk89q.worldedit.entity.Player player : players) {
            if (player instanceof BukkitPlayer) {
                Player bPlayer = ((BukkitPlayer) player).getPlayer();
                bPlayer.teleport(getRandomDest());
            }
        }
    }

    @Override
    public void run() {
        if ((!isBossSpawned() && lastDeath != 0) || emptyTicks > 60) {
            if (System.currentTimeMillis() - lastDeath >= 1000 * 60 * 5) {
                expire();
            }
        } else {
            if (isEmpty()) {
                ++emptyTicks;
            } else {
                emptyTicks = 0;
                PatientXConfig config = getMaster().getConfig();

                equalize();
                teleportRandom();
                freezeEntities();
                freezeBlocks(ChanceUtil.getChance((int) Math.ceil(config.iceChangeChance - difficulty)));
                spawnCreatures();
                printBossHealth();
            }
        }
    }

    public boolean isBossSpawned() {
        boss = null;
        getContained(Zombie.class).stream().filter(Entity::isValid).filter(z -> !z.isValid()).forEach(e -> {
            Boss b = getMaster().getBossManager().updateLookup(e);
            if (b == null) {
                e.remove();
            } else {
                boss = b;
            }
        });
        return boss != null;
    }

    public void spawnBoss() {
        Location l = LocationUtil.getCenter(getBukkitWorld(), getRegion());
        boss = new Boss(getBukkitWorld().spawn(l, Giant.class), new SBossDetail(this));
        getMaster().getBossManager().bind(boss);
    }

    public void bossDied() {
        lastDeath = System.currentTimeMillis();
        boss = null;
    }

    public void equalize() {
        AdminComponent admin = getMaster().getAdmin();
        PatientXConfig config = getMaster().getConfig();

        for (Player player : getContained(Player.class)) {
            admin.deadmin(player);

            if (player.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) {
                ChatUtil.sendWarning(player, "Your defensive potion enrages me!");
                modifyDifficulty(1);
                player.damage(difficulty * config.baseBossHit, boss.getEntity());
            }

            Entity vehicle = player.getVehicle();
            if (vehicle != null && !(vehicle instanceof Bat)) {
                vehicle.eject();
                ChatUtil.sendWarning(player, "Patient X throws you off!");
            }
        }
    }

    protected void teleportRandom() {
        teleportRandom(false);
    }

    protected void teleportRandom(boolean force) {
        long diff = System.currentTimeMillis() - lastTelep;
        if (!force) {
            if (!ChanceUtil.getChance(4) || (lastTelep != 0 && diff < 8000)) return;
        }

        lastTelep = System.currentTimeMillis();

        boss.getEntity().teleport(getRandomDest());
        ChatUtil.sendNotice(getContained(Player.class), "Pause for a second chap, I need to answer the teleport!");
    }

    private void freezeEntities() {
        LivingEntity boss = this.boss.getEntity();

        double total = 0;
        for (LivingEntity entity : getMaster().getToolKit().removeAdmin(getContained(LivingEntity.class))) {
            if (entity.equals(boss)) continue;
            if (!EnvironmentUtil.isWater(entity.getLocation().getBlock())) {
                continue;
            }
            if (entity instanceof Zombie) {
                entity.setHealth(0);
                EntityUtil.heal(boss, 1);
                total += .02;
            } else if (!ChanceUtil.getChance(5)) {
                entity.damage(ChanceUtil.getRandom(25));
            }
        }
        modifyDifficulty(-total);
    }

    public void freezeBlocks(boolean throwExplosives) {
        freezeBlocks(getMaster().getConfig().iceChance, throwExplosives);
    }

    public void freezeBlocks(int percentage, boolean throwExplosives) {
        PatientXConfig config = getMaster().getConfig();

        int minX = ice.getMinimumPoint().getBlockX();
        int maxX = ice.getMaximumPoint().getBlockX();
        int minZ = ice.getMinimumPoint().getBlockZ();
        int maxZ = ice.getMaximumPoint().getBlockZ();
        int y = ice.getMaximumPoint().getBlockY();

        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                Block block = getBukkitWorld().getBlockAt(x, y, z);
                if (block.getRelative(BlockFace.UP).getTypeId() == 0
                        && EnvironmentUtil.isWater(block.getRelative(BlockFace.DOWN))) {
                    if (percentage >= 100) {
                        block.setTypeId(BlockID.ICE);
                        continue;
                    }
                    if (block.getTypeId() == BlockID.PACKED_ICE || block.getTypeId() == BlockID.ICE) {
                        block.setTypeId(BlockID.STATIONARY_WATER);
                        if (!ChanceUtil.getChance(config.snowBallChance) || !throwExplosives) continue;
                        Location target = block.getRelative(BlockFace.UP).getLocation();
                        for (int i = ChanceUtil.getRandom(3); i > 0; i--) {
                            Snowball melvin = getBukkitWorld().spawn(target, Snowball.class);
                            melvin.setVelocity(new Vector(0, ChanceUtil.getRangedRandom(.25, 1), 0));
                            melvin.setShooter(boss.getEntity());
                        }
                    } else if (ChanceUtil.getChance(percentage, 100)) {
                        block.setTypeId(BlockID.PACKED_ICE);
                    }
                }
            }
        }
    }

    public void spawnCreatures() {
        LivingEntity boss = this.boss.getEntity();

        Collection<LivingEntity> entities = getMaster().getToolKit().removeAdmin(getContained(LivingEntity.class));
        if (entities.size() > 500) {
            ChatUtil.sendWarning(getContained(Player.class), "Ring-a-round the rosie, a pocket full of posies...");
            boss.setHealth(boss.getMaxHealth());
            for (Entity entity : entities) {
                if (entity instanceof Player) {
                    ((Player) entity).setHealth(0);
                } else if (!entity.equals(boss)) {
                    entity.remove();
                }
            }
            return;
        }

        double amt = getMaster().getToolKit().removeAdmin(getContained(Player.class)).size() * difficulty;
        Location l = getCentralLoc();
        for (int i = 0; i < amt; i++) {
            Zombie zombie = getBukkitWorld().spawn(l, Zombie.class);
            zombie.setCanPickupItems(false);
            zombie.setBaby(true);
        }
    }

    public void printBossHealth() {
        LivingEntity boss = this.boss.getEntity();
        PatientXConfig config = getMaster().getConfig();

        int current = (int) Math.ceil(boss.getHealth());
        int max = (int) Math.ceil(boss.getMaxHealth());

        String message = "Boss Health: " + current + " / " + max;
        double maxDiff = config.maxDifficulty - config.minDifficulty;
        double curDiff = difficulty - config.minDifficulty;
        message += " Enragement: " + (int) Math.round((curDiff / maxDiff) * 100) + "%";
        ChatUtil.sendNotice(getContained(Player.class), ChatColor.DARK_AQUA, message);
    }
}
