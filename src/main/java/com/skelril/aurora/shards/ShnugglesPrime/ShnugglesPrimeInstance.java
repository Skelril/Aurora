/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shards.ShnugglesPrime;

import com.sk89q.craftbook.bukkit.BukkitPlayer;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.ItemID;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.skelril.OpenBoss.Boss;
import com.skelril.OpenBoss.EntityDetail;
import com.skelril.aurora.bosses.detail.SBossDetail;
import com.skelril.aurora.events.anticheat.ThrowPlayerEvent;
import com.skelril.aurora.exceptions.UnsupportedPrayerException;
import com.skelril.aurora.prayer.PrayerComponent;
import com.skelril.aurora.prayer.PrayerType;
import com.skelril.aurora.shard.ShardInstance;
import com.skelril.aurora.shards.BukkitShardInstance;
import com.skelril.aurora.util.ChanceUtil;
import com.skelril.aurora.util.ChatUtil;
import com.skelril.aurora.util.EntityUtil;
import com.skelril.aurora.util.LocationUtil;
import com.skelril.aurora.util.timer.IntegratedRunnable;
import com.skelril.aurora.util.timer.TimedRunnable;
import com.skelril.aurora.util.timer.TimerUtil;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import static com.sk89q.commandbook.CommandBook.inst;
import static com.sk89q.commandbook.CommandBook.logger;
import static com.zachsthings.libcomponents.bukkit.BasePlugin.server;

public class ShnugglesPrimeInstance extends BukkitShardInstance<ShnugglesPrimeShard> implements Runnable {

    private Giant boss = null;
    private long lastAttack = 0;
    private int lastAttackNumber = -1;
    private long lastDeath = 0;
    private boolean damageHeals = false;
    private Random random = new Random();

    private long lastUltimateAttack = -1;
    private boolean flagged = false;

    private double toHeal = 0;
    private List<Location> spawnPts = new ArrayList<>();

    public ShnugglesPrimeInstance(ShnugglesPrimeShard shard, World world, ProtectedRegion region) {
        super(shard, world, region);
        probeArea();
    }

    public void probeArea() {
        spawnPts.clear();
        BlockVector min = getRegion().getParent().getMinimumPoint();
        BlockVector max = getRegion().getParent().getMaximumPoint();
        int minX = min.getBlockX();
        int minZ = min.getBlockZ();
        int minY = min.getBlockY();
        int maxX = max.getBlockX();
        int maxZ = max.getBlockZ();
        int maxY = max.getBlockY();
        BlockState block;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = maxY; y >= minY; --y) {
                    block = getBukkitWorld().getBlockAt(x, y, z).getState();
                    if (!block.getChunk().isLoaded()) block.getChunk().load();
                    if (block.getTypeId() == BlockID.GOLD_BLOCK) {
                        spawnPts.add(block.getLocation().add(0, 2, 0));
                    }
                }
            }
        }
    }

    public static ShnugglesPrimeInstance getInst(EntityDetail detail) {
        if (detail instanceof SBossDetail) {
            ShardInstance<?> inst = ((SBossDetail) detail).getInstance();
            if (inst instanceof ShnugglesPrimeInstance) {
                return (ShnugglesPrimeInstance) inst;
            }
        }
        return null;
    }

    @Override
    public void teleportTo(com.sk89q.worldedit.entity.Player... players) {
        Location target = LocationUtil.getCenter(getBukkitWorld(), region);
        for (com.sk89q.worldedit.entity.Player player : players) {
            if (player instanceof BukkitPlayer) {
                Player bPlayer = ((BukkitPlayer) player).getPlayer();
                bPlayer.teleport(target);
            }
        }
    }

    @Override
    public void run() {
        if (!isBossSpawned()) {
            if (lastDeath == 0 || System.currentTimeMillis() - lastDeath >= 1000 * 60 * 3) {
                removeMobs();
                spawnBoss();
            }
        } else if (!isEmpty()) {
            equalize();
            runAttack(ChanceUtil.getRandom(OPTION_COUNT));
        }
    }

    public Giant getBoss() {
        return boss;
    }

    public void healBoss(float percentHealth) {
        if (isBossSpawned()) {
            EntityUtil.heal(boss, boss.getMaxHealth() * percentHealth);
        }
    }

    public void removeMobs() {
        getContained(1, Monster.class).forEach(e -> {
            for (int i = 0; i < 20; i++) getBukkitWorld().playEffect(e.getLocation(), Effect.SMOKE, 0);
            e.remove();
        });
    }

    public void buffBabies() {
        for (Zombie zombie : getContained(Zombie.class)) {
            zombie.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20 * 20, 3), true);
        }
    }

    public boolean isBossSpawned() {
        if (!isActive()) return true;
        getContained(Giant.class).stream().filter(Entity::isValid).forEach(e -> {
            Boss b = getMaster().getBossManager().updateLookup(e);
            if (b == null) {
                e.remove();
            }
        });
        return boss != null && boss.isValid();
    }

    public void spawnBoss() {
        BlockVector min = getRegion().getMinimumPoint();
        BlockVector max = getRegion().getMaximumPoint();
        Region region = new CuboidRegion(min, max);
        Location l = BukkitUtil.toLocation(getBukkitWorld(), region.getCenter());
        boss = getBukkitWorld().spawn(l, Giant.class);
        getMaster().getBossManager().bind(new Boss(boss, new SBossDetail(this)));
    }

    public void bossDied() {
        lastDeath = System.currentTimeMillis();
        boss = null;
    }

    public void equalize() {
        flagged = false;
        // Equalize Players
        for (Player player : getContained(Player.class)) {
            try {
                getMaster().getAdmin().standardizePlayer(player);
                if (player.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) {
                    flagged = true;
                }
                if (player.getVehicle() != null) {
                    player.getVehicle().eject();
                    ChatUtil.sendWarning(player, "The boss throws you off!");
                }
            } catch (Exception e) {
                logger().warning("The player: " + player.getName() + " may have an unfair advantage.");
            }
        }
    }

    public boolean damageHeals() {
        return damageHeals;
    }

    public boolean canUseUltimate(long time) {
        return System.currentTimeMillis() - lastUltimateAttack >= time;
    }

    public void updateLastUltimate() {
        lastUltimateAttack = System.currentTimeMillis();
    }

    public int getLastAttack() {
        return lastAttack <= 13000 ? lastAttackNumber : -1;
    }

    public void printBossHealth() {
        int current = (int) Math.ceil(boss.getHealth());
        int max = (int) Math.ceil(boss.getMaxHealth());
        String message = "Boss Health: " + current + " / " + max;
        ChatUtil.sendNotice(getContained(Player.class), ChatColor.DARK_AQUA, message);
    }

    private static final ItemStack weapon = new ItemStack(ItemID.BONE);

    static {
        ItemMeta weaponMeta = weapon.getItemMeta();
        weaponMeta.addEnchant(Enchantment.DAMAGE_ALL, 2, true);
        weapon.setItemMeta(weaponMeta);
    }

    public void spawnMinions(LivingEntity target) {
        int spawnCount = Math.max(3, getContained(Player.class).size());
        for (Location spawnPt : spawnPts) {
            if (ChanceUtil.getChance(11)) {
                for (int i = spawnCount; i > 0; --i) {
                    Zombie z = getBukkitWorld().spawn(spawnPt, Zombie.class);
                    z.setBaby(true);
                    EntityEquipment equipment = z.getEquipment();
                    equipment.setArmorContents(null);
                    equipment.setItemInHand(weapon.clone());
                    equipment.setItemInHandDropChance(0F);
                    if (target != null) {
                        z.setTarget(target);
                    }
                }
            }
        }
    }

    public static final int OPTION_COUNT = 9;

    public void runAttack(int attackCase) {
        int delay = ChanceUtil.getRangedRandom(13000, 17000);
        if (lastAttack != 0 && System.currentTimeMillis() - lastAttack <= delay) return;
        Collection<Player> containedP = getContained(1, Player.class);
        Collection<Player> contained = getContained(Player.class);
        if (contained == null || contained.size() <= 0) return;
        if (attackCase < 1 || attackCase > OPTION_COUNT) attackCase = ChanceUtil.getRandom(OPTION_COUNT);
        // AI system
        if ((attackCase == 5 || attackCase == 9) && boss.getHealth() > boss.getMaxHealth() * .9) {
            attackCase = ChanceUtil.getChance(2) ? 8 : 2;
        }
        if (flagged && ChanceUtil.getChance(4)) {
            attackCase = ChanceUtil.getChance(2) ? 4 : 7;
        }
        for (Player player : contained) {
            if (player.getHealth() < 4) {
                attackCase = 2;
                break;
            }
        }
        if (boss.getHealth() < boss.getMaxHealth() * .3 && ChanceUtil.getChance(2)) {
            attackCase = 9;
        }
        if (getContained(Zombie.class).size() > 200) {
            attackCase = 7;
        }
        if ((attackCase == 3 || attackCase == 6) && boss.getHealth() < boss.getMaxHealth() * .6) {
            runAttack(ChanceUtil.getRandom(OPTION_COUNT));
            return;
        }
        switch (attackCase) {
            case 1:
                ChatUtil.sendWarning(containedP, "Taste my wrath!");
                for (Player player : contained) {
                    // Call this event to notify AntiCheat
                    server().getPluginManager().callEvent(new ThrowPlayerEvent(player));
                    player.setVelocity(new Vector(
                            random.nextDouble() * 3 - 1.5,
                            random.nextDouble() * 1 + 1.3,
                            random.nextDouble() * 3 - 1.5
                    ));
                    player.setFireTicks(20 * 3);
                }
                break;
            case 2:
                ChatUtil.sendWarning(containedP, "Embrace my corruption!");
                for (Player player : contained) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 20 * 12, 1));
                }
                break;
            case 3:
                ChatUtil.sendWarning(containedP, "Are you BLIND? Mwhahahaha!");
                for (Player player : contained) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 4, 0));
                }
                break;
            case 4:
                ChatUtil.sendWarning(containedP, ChatColor.DARK_RED + "Tango time!");
                server().getScheduler().runTaskLater(inst(), () -> {
                    if (!isBossSpawned()) return;
                    for (Player player : getContained(Player.class)) {
                        if (boss.hasLineOfSight(player)) {
                            ChatUtil.sendNotice(player, "Come closer...");
                            player.teleport(boss.getLocation());
                            player.damage(100, boss);
                            // Call this event to notify AntiCheat
                            server().getPluginManager().callEvent(new ThrowPlayerEvent(player));
                            player.setVelocity(new Vector(
                                    random.nextDouble() * 1.7 - 1.5,
                                    random.nextDouble() * 2,
                                    random.nextDouble() * 1.7 - 1.5
                            ));
                        } else {
                            ChatUtil.sendNotice(player, "Fine... No tango this time...");
                        }
                    }
                    ChatUtil.sendNotice(getContained(1, Player.class), "Now wasn't that fun?");
                }, 20 * 7);
                break;
            case 5:
                if (!damageHeals) {
                    ChatUtil.sendWarning(containedP, "I am everlasting!");
                    damageHeals = true;
                    server().getScheduler().runTaskLater(inst(), () -> {
                        if (damageHeals) {
                            damageHeals = false;
                            if (!isBossSpawned()) return;
                            ChatUtil.sendNotice(getContained(1, Player.class), "Thank you for your assistance.");
                        }
                    }, 20 * 12);
                    break;
                }
                runAttack(ChanceUtil.getRandom(OPTION_COUNT));
                return;
            case 6:
                ChatUtil.sendWarning(containedP, "Fire is your friend...");
                for (Player player : contained) {
                    player.setFireTicks(20 * 30);
                }
                break;
            case 7:
                ChatUtil.sendWarning(containedP, ChatColor.DARK_RED + "Bask in my glory!");
                server().getScheduler().runTaskLater(inst(), () -> {
                    if (!isBossSpawned()) return;
                    // Set defaults
                    boolean baskInGlory = getContained(Player.class).size() == 0;
                    damageHeals = true;
                    // Check Players
                    for (Player player : getContained(Player.class)) {
                        if (inst().hasPermission(player, "aurora.prayer.intervention") && ChanceUtil.getChance(3)) {
                            ChatUtil.sendNotice(player, "A divine wind hides you from the boss.");
                            continue;
                        }
                        if (boss.hasLineOfSight(player)) {
                            ChatUtil.sendWarning(player, ChatColor.DARK_RED + "You!");
                            baskInGlory = true;
                        }
                    }
                    //Attack
                    if (baskInGlory) {
                        spawnPts.stream().filter(pt -> ChanceUtil.getChance(12)).forEach(pt -> {
                            getBukkitWorld().createExplosion(pt.getX(), pt.getY(), pt.getZ(), 10, false, false);
                        });
                        //Schedule Reset
                        server().getScheduler().runTaskLater(inst(), () -> damageHeals = false, 10);
                        return;
                    }
                    // Notify if avoided
                    ChatUtil.sendNotice(getContained(1, Player.class), "Gah... Afraid are you friends?");
                }, 20 * 7);
                break;
            case 8:
                ChatUtil.sendWarning(containedP, ChatColor.DARK_RED + "I ask thy lord for aid in this all mighty battle...");
                ChatUtil.sendWarning(containedP, ChatColor.DARK_RED + "Heed thy warning, or perish!");
                server().getScheduler().runTaskLater(inst(), () -> {
                    if (!isBossSpawned()) return;
                    ChatUtil.sendWarning(getContained(1, Player.class), "May those who appose me die a death like no other...");
                    getContained(Player.class).stream().filter(boss::hasLineOfSight).forEach(player -> {
                        ChatUtil.sendWarning(getContained(1, Player.class), "Perish " + player.getName() + "!");
                        try {
                            getMaster().getPrayers().influencePlayer(
                                    player,
                                    PrayerComponent.constructPrayer(player, PrayerType.DOOM, 120000)
                            );
                        } catch (UnsupportedPrayerException e) {
                            e.printStackTrace();
                        }
                    });
                }, 20 * 7);
                break;
            case 9:
                ChatUtil.sendNotice(containedP, ChatColor.DARK_RED, "My minions our time is now!");
                IntegratedRunnable minionEater = new IntegratedRunnable() {
                    @Override
                    public boolean run(int times) {
                        if (!isBossSpawned()) return true;
                        for (LivingEntity entity : getContained(LivingEntity.class)) {
                            if (entity instanceof Giant || !ChanceUtil.getChance(5)) continue;
                            double realDamage = entity.getHealth();
                            if (entity instanceof Zombie && ((Zombie) entity).isBaby()) {
                                entity.setHealth(0);
                            } else {
                                entity.damage(realDamage, boss);
                            }
                            toHeal += realDamage / 3;
                        }
                        if (TimerUtil.matchesFilter(times + 1, -1, 2)) {
                            ChatUtil.sendNotice(getContained(1, Player.class), ChatColor.DARK_AQUA, "The boss has drawn in: " + (int) toHeal + " health.");
                        }
                        return true;
                    }

                    @Override
                    public void end() {
                        if (!isBossSpawned()) return;
                        boss.setHealth(Math.min(toHeal + boss.getHealth(), boss.getMaxHealth()));
                        toHeal = 0;
                        ChatUtil.sendNotice(getContained(1, Player.class), "Thank you my minions!");
                        printBossHealth();
                    }
                };
                TimedRunnable minonEatingTask = new TimedRunnable(minionEater, 20);
                BukkitTask minionEatingTaskExecutor = server().getScheduler().runTaskTimer(inst(), minonEatingTask, 0, 10);
                minonEatingTask.setTask(minionEatingTaskExecutor);
                break;
        }
        lastAttack = System.currentTimeMillis();
        lastAttackNumber = attackCase;
    }
}
