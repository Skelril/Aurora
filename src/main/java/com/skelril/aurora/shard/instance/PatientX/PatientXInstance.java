/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shard.instance.PatientX;

import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.skelril.OpenBoss.Boss;
import com.skelril.OpenBoss.EntityDetail;
import com.skelril.aurora.admin.AdminComponent;
import com.skelril.aurora.combat.bosses.detail.SBossDetail;
import com.skelril.aurora.shard.ShardInstance;
import com.skelril.aurora.shard.instance.BukkitShardInstance;
import com.skelril.aurora.util.*;
import com.skelril.aurora.util.player.AdminToolkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import static com.sk89q.commandbook.CommandBook.inst;
import static com.zachsthings.libcomponents.bukkit.BasePlugin.server;

public class PatientXInstance extends BukkitShardInstance<PatientXShard> implements Runnable {

    private CuboidRegion ice, drops;

    private Boss boss = null;
    private long attackDur = 0;
    private int lastAttack = 0;
    private long lastUltimateAttack = 0;
    private long lastDeath = 0;
    private long lastTelep = 0;
    private int emptyTicks = 0;
    private int activeTicks = 0;
    private double difficulty;
    private Random random = new Random();

    private List<Location> destinations = new ArrayList<>();

    public PatientXInstance(PatientXShard shard, World world, ProtectedRegion region) {
        super(shard, world, region);
        setUp();
        remove();
        spawnBoss();
    }

    public void setUp() {
        com.sk89q.worldedit.Vector min = getRegion().getMinimumPoint();
        org.bukkit.World bWorld = getBukkitWorld();
        destinations.add(new Location(bWorld, min.getX() + 11.5, min.getY() + 25, min.getZ() + 34.5));
        destinations.add(new Location(bWorld, min.getX() + 10.5, min.getY() + 20, min.getZ() + 34.5));
        destinations.add(new Location(bWorld, min.getX() + 57.5, min.getY() + 25, min.getZ() + 34.5));
        destinations.add(new Location(bWorld, min.getX() + 63.5, min.getY() + 25, min.getZ() + 45.5));
        destinations.add(new Location(bWorld, min.getX() + 62.5, min.getY() + 18, min.getZ() + 34.5));
        destinations.add(new Location(bWorld, min.getX() + 34.5, min.getY() + 29, min.getZ() + 60.5));
        destinations.add(new Location(bWorld, min.getX() + 34.5, min.getY() + 18, min.getZ() + 34.5));
        destinations.add(new Location(bWorld, min.getX() + 24.5, min.getY() + 29, min.getZ() + 41.5));
        destinations.add(getCenter());

        ice = new CuboidRegion(min.add(14, 17, 14), min.add(62, 17, 58));
        drops = new CuboidRegion(min.add(14, 24, 14), min.add(54, 24, 54));
    }

    public double getDifficulty() {
        return difficulty;
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

    public static PatientXInstance getInst(EntityDetail detail) {
        if (detail instanceof SBossDetail) {
            ShardInstance<?> inst = ((SBossDetail) detail).getInstance();
            if (inst instanceof PatientXInstance) {
                return (PatientXInstance) inst;
            }
        }
        return null;
    }

    public CuboidRegion getDropRegion() {
        return drops;
    }

    @Override
    public void teleportTo(com.sk89q.worldedit.entity.Player... players) {
        for (com.sk89q.worldedit.entity.Player player : players) {
            if (player instanceof BukkitPlayer) {
                Player bPlayer = ((BukkitPlayer) player).getPlayer();
                do {
                    bPlayer.teleport(getRandomDest());
                } while (getBoss() != null && getBoss().hasLineOfSight(bPlayer));
            }
        }
    }

    @Override
    public void cleanUp() {
        if (boss != null) {
            getMaster().getBossManager().silentUnbind(boss);
        }
        super.cleanUp();
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
                ++activeTicks;

                if (activeTicks % 8 == 0) {
                    PatientXConfig config = getMaster().getConfig();

                    equalize();
                    teleportRandom();
                    freezeEntities();
                    freezeBlocks(ChanceUtil.getChance((int) Math.ceil(config.iceChangeChance - difficulty)));
                    spawnCreatures();
                    printBossHealth();
                }

                if (activeTicks % 20 == 0) {
                    runAttack();
                }
            }
        }
    }

    public boolean isBossSpawned() {
        getContained(Zombie.class).stream().filter(Entity::isValid).filter(z -> !z.isBaby()).forEach(e -> {
            Boss b = getMaster().getBossManager().updateLookup(e);
            if (b == null) {
                e.remove();
            }
        });
        return boss != null;
    }

    public void spawnBoss() {
        resetDifficulty();
        freezeBlocks(false);

        boss = new Boss(getBukkitWorld().spawn(getCenter(), Zombie.class), new SBossDetail(this));
        getMaster().getBossManager().bind(boss);
    }

    public Location getCenter() {
        com.sk89q.worldedit.Vector min = getRegion().getMinimumPoint();
        org.bukkit.World bWorld = getBukkitWorld();
        return new Location(bWorld, min.getX() + 34.5, min.getY() + 25, min.getZ() + 34.5);

    }

    public LivingEntity getBoss() {
        return boss == null ? null : boss.getEntity();
    }

    public void healBoss(float percentHealth) {
        if (isBossSpawned()) {
            LivingEntity boss = getBoss();
            EntityUtil.heal(boss, boss.getMaxHealth() * percentHealth);
        }
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
                player.damage(difficulty * config.baseBossHit, getBoss());
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
        LivingEntity boss = getBoss();

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
                            melvin.setShooter(getBoss());
                        }
                    } else if (ChanceUtil.getChance(percentage, 100)) {
                        block.setTypeId(BlockID.PACKED_ICE);
                    }
                }
            }
        }
    }

    public void spawnCreatures() {
        LivingEntity boss = getBoss();

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
        Location l = getCenter();
        for (int i = 0; i < amt; i++) {
            Zombie zombie = getBukkitWorld().spawn(l, Zombie.class);
            zombie.setCanPickupItems(false);
            zombie.setBaby(true);
        }
    }

    public boolean canUseUltimate(long time) {
        return System.currentTimeMillis() - lastUltimateAttack >= time;
    }

    public void updateLastUltimate() {
        lastUltimateAttack = System.currentTimeMillis();
    }

    public int getLastAttack() {
        return System.currentTimeMillis() > attackDur ? 0 : lastAttack;
    }

    public void printBossHealth() {
        LivingEntity boss = getBoss();
        PatientXConfig config = getMaster().getConfig();

        int current = (int) Math.ceil(boss.getHealth());
        int max = (int) Math.ceil(boss.getMaxHealth());

        String message = "Boss Health: " + current + " / " + max;
        double maxDiff = config.maxDifficulty - config.minDifficulty;
        double curDiff = difficulty - config.minDifficulty;
        message += " Enragement: " + (int) Math.round((curDiff / maxDiff) * 100) + "%";
        ChatUtil.sendNotice(getContained(Player.class), ChatColor.DARK_AQUA, message);
    }

    public static final int OPTION_COUNT = 9;

    public void runAttack() {
        runAttack(0);
    }

    public void runAttack(int attackCase) {

        if (!isBossSpawned()) return;

        AdminToolkit adminKit = getMaster().getToolKit();
        PatientXConfig config = getMaster().getConfig();

        Collection<Player> spectator = getContained(Player.class);
        Collection<Player> contained = adminKit.removeAdmin(spectator);
        if (contained.isEmpty()) return;

        LivingEntity boss = getBoss();

        if (attackCase < 1 || attackCase > OPTION_COUNT) attackCase = ChanceUtil.getRandom(OPTION_COUNT);

        switch (attackCase) {
            case 1:
                ChatUtil.sendWarning(spectator, "Let's play musical chairs!");
                for (Player player : contained) {
                    do {
                        player.teleport(getRandomDest());
                    } while (player.getLocation().distanceSquared(boss.getLocation()) <= 5 * 5);
                    if (boss.hasLineOfSight(player)) {
                        player.setHealth(ChanceUtil.getRandom(player.getMaxHealth()));
                        ChatUtil.sendWarning(player, "Don't worry, I have a medical degree...");
                        ChatUtil.sendWarning(player, "...or was that a certificate of insanity?");
                    }
                }
                attackDur = System.currentTimeMillis() + 2000;
                break;
            case 2:
                for (Player player : contained) {
                    final double old = player.getHealth();
                    player.setHealth(3);
                    server().getScheduler().runTaskLater(inst(), () -> {
                        if (!player.isValid() || !contains(player)) return;
                        player.setHealth(old * .75);
                    }, 20 * 2);
                }
                attackDur = System.currentTimeMillis() + 3000;
                ChatUtil.sendWarning(spectator, "This special attack will be a \"smashing hit\"!");
                break;
            case 3:
                double tntQuantity = Math.max(2, difficulty / 2.4);
                for (Player player : contained) {
                    for (double i = ChanceUtil.getRangedRandom(tntQuantity, Math.pow(2, Math.min(9, tntQuantity))); i > 0; i--) {
                        Entity e = getBukkitWorld().spawn(player.getLocation(), TNTPrimed.class);
                        e.setVelocity(new org.bukkit.util.Vector(
                                random.nextDouble() * 1 - .5,
                                random.nextDouble() * .8 + .2,
                                random.nextDouble() * 1 - .5
                        ));
                    }
                }
                attackDur = System.currentTimeMillis() + 5000;
                ChatUtil.sendWarning(spectator, "Your performance is really going to \"bomb\"!");
                break;
            case 4:
                for (Player player : contained) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 20 * 15, 1));
                }
                attackDur = System.currentTimeMillis() + 15750;
                ChatUtil.sendWarning(spectator, "Like a candle I hope you don't \"whither\" and die!");
                break;
            case 5:
                for (Player player : contained) {
                    for (int i = ChanceUtil.getRandom(6) + 2; i > 0; --i) {
                        DeathUtil.throwSlashPotion(player.getLocation());
                    }
                }
                attackDur = System.currentTimeMillis() + 2000;
                ChatUtil.sendWarning(spectator, "Splash to it!");
                break;
            case 6:
                for (Player player : contained) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 60, 2));
                }
                attackDur = System.currentTimeMillis() + 20000;
                ChatUtil.sendWarning(spectator, "What's the matter, got cold feet?");
                break;
            case 7:
                for (Player player : contained) {
                    player.chat("I love Patient X!");
                    Bat b = getBukkitWorld().spawn(player.getLocation(), Bat.class);
                    b.setPassenger(player);
                }
                attackDur = System.currentTimeMillis() + 20000;
                ChatUtil.sendWarning(spectator, "Awe, I love you too!");
                ChatUtil.sendWarning(spectator, "But only cause I'm a little batty...");
                break;
            case 8:
                server().getScheduler().runTaskLater(inst(), () -> {
                    for (int i = config.radiationTimes; i > 0; i--) {
                        server().getScheduler().runTaskLater(inst(), () -> {
                            if (boss != null) {
                                for (Player player : adminKit.removeAdmin(getContained(Player.class))) {
                                    for (int e = 0; e < 3; ++e) {
                                        Location t = LocationUtil.findRandomLoc(player.getLocation(), 5, true);
                                        for (int k = 0; k < 10; ++k) {
                                            getBukkitWorld().playEffect(t, Effect.MOBSPAWNER_FLAMES, 0);
                                        }
                                    }
                                    if (player.getLocation().getBlock().getLightLevel() >= config.radiationLightLevel) {
                                        player.damage(difficulty * config.radiationMultiplier);
                                    }
                                }
                            }
                        }, i * 10);
                    }
                }, 3 * 20);
                attackDur = System.currentTimeMillis() + (config.radiationTimes * 500);
                ChatUtil.sendWarning(spectator, "Ahhh not the radiation treatment!");
                break;
            case 9:
                final int burst = ChanceUtil.getRangedRandom(10, 20);
                server().getScheduler().runTaskLater(inst(), () -> {
                    for (int i = burst; i > 0; i--) {
                        server().getScheduler().runTaskLater(inst(), () -> {
                            if (boss != null) freezeBlocks(true);
                        }, i * 10);
                    }
                }, 7 * 20);
                attackDur = System.currentTimeMillis() + 7000 + (500 * burst);
                ChatUtil.sendWarning(spectator, "Let's have a snow ball fight!");
                break;
        }
        lastAttack = attackCase;
    }
}
