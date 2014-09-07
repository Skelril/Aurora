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

package com.skelril.aurora.shard.instance.ShnugglesPrime;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.ItemID;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.skelril.OpenBoss.Boss;
import com.skelril.OpenBoss.EntityDetail;
import com.skelril.aurora.combat.bosses.detail.SBossDetail;
import com.skelril.aurora.events.anticheat.ThrowPlayerEvent;
import com.skelril.aurora.exceptions.UnsupportedPrayerException;
import com.skelril.aurora.prayer.PrayerComponent;
import com.skelril.aurora.prayer.PrayerType;
import com.skelril.aurora.shard.ShardInstance;
import com.skelril.aurora.shard.instance.BukkitShardInstance;
import com.skelril.aurora.util.*;
import com.skelril.aurora.util.timer.IntegratedRunnable;
import com.skelril.aurora.util.timer.TimedRunnable;
import com.skelril.aurora.util.timer.TimerUtil;
import org.bukkit.ChatColor;
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

import java.util.*;

import static com.sk89q.commandbook.CommandBook.inst;
import static com.sk89q.commandbook.CommandBook.logger;
import static com.zachsthings.libcomponents.bukkit.BasePlugin.server;

public class ShnugglesPrimeInstance extends BukkitShardInstance<ShnugglesPrimeShard> implements Runnable {

    private Boss boss = null;
    private long lastAttack = 0;
    private int lastAttackNumber = -1;
    private long lastDeath = 0;
    private boolean damageHeals = false;
    private Set<Integer> activeAttacks = new HashSet<>();
    private Random random = new Random();

    private long lastUltimateAttack = -1;
    private boolean flagged = false;
    private int emptyTicks = 0;

    private double toHeal = 0;
    private List<Location> spawnPts = new ArrayList<>();

    public ShnugglesPrimeInstance(ShnugglesPrimeShard shard, World world, ProtectedRegion region) {
        super(shard, world, region);
        probeArea();
        remove();
        spawnBoss();
    }

    public void probeArea() {
        spawnPts.clear();
        BlockVector min = getRegion().getMinimumPoint();
        BlockVector max = getRegion().getMaximumPoint();
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
                        Location target = block.getLocation().add(0, 2, 0);
                        if (target.getBlock().isEmpty()) {
                            spawnPts.add(target);
                        }
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
        Location target;
        for (com.sk89q.worldedit.entity.Player player : players) {
            if (player instanceof BukkitPlayer) {
                Player bPlayer = ((BukkitPlayer) player).getPlayer();
                do {
                    target = CollectionUtil.getElement(spawnPts);
                } while (getBoss() != null && (target.distanceSquared(getBoss().getLocation()) < 7 * 7));
                bPlayer.teleport(target);
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
            if (System.currentTimeMillis() - lastDeath >= 1000 * 60 * 3) {
                expire();
            }
        } else {
            if (isEmpty()) {
                ++emptyTicks;
            } else {
                emptyTicks = 0;
                equalize();
                requestXPCleanup();
                runAttack(ChanceUtil.getRandom(OPTION_COUNT));
            }
        }
    }

    public void buffBabies() {
        for (Zombie zombie : getContained(Zombie.class)) {
            zombie.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20 * 20, 3), true);
        }
    }

    public boolean isBossSpawned() {
        getContained(Giant.class).stream().filter(Entity::isValid).forEach(e -> {
            Boss b = getMaster().getBossManager().updateLookup(e);
            if (b == null) {
                e.remove();
            }
        });
        return boss != null;
    }

    public void spawnBoss() {
        Location l = LocationUtil.getCenter(getBukkitWorld(), getRegion());
        boss = new Boss(getBukkitWorld().spawn(l, Giant.class), new SBossDetail(this));
        getMaster().getBossManager().bind(boss);
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
        activeAttacks.clear();
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

    public void requestXPCleanup() {
        getContained(ExperienceOrb.class).stream().filter(e -> e.getTicksLived() > 20 * 13).forEach(Entity::remove);
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
        return lastAttack + 13000 > System.currentTimeMillis() ? lastAttackNumber : -1;
    }

    public boolean isActiveAttack(Integer attack) {
        return activeAttacks.contains(attack);
    }

    public void printBossHealth() {
        LivingEntity boss = getBoss();
        if (boss == null) return;
        int current = (int) Math.ceil(boss.getHealth());
        int max = (int) Math.ceil(boss.getMaxHealth());
        String message = "Boss Health: " + current + " / " + max;
        ChatUtil.send(getContained(Player.class), ChatColor.DARK_AQUA, message);
    }

    private static final ItemStack weapon = new ItemStack(ItemID.BONE);

    static {
        ItemMeta weaponMeta = weapon.getItemMeta();
        weaponMeta.addEnchant(Enchantment.DAMAGE_ALL, 2, true);
        weapon.setItemMeta(weaponMeta);
    }

    public void spawnMinions(LivingEntity target) {
        int spawnCount = Math.max(3, getMaster().getToolKit().removeAdmin(getContained(Player.class)).size());
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
        LivingEntity boss = getBoss();
        double delay = Math.max(5000, ChanceUtil.getRangedRandom(15 * boss.getHealth(), 25 * boss.getHealth()));
        if (lastAttack != 0 && System.currentTimeMillis() - lastAttack <= delay) return;
        Collection<Player> spectator = getContained(Player.class);
        Collection<Player> contained = getMaster().getToolKit().removeAdmin(spectator);
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
        Collection<Zombie> zombies = getContained(Zombie.class);
        if (zombies.size() > 200) {
            attackCase = 7;
        }
        if (boss.getHealth() < boss.getMaxHealth() * .4 && ChanceUtil.getChance(5)) {
            if (zombies.size() < 100 && boss.getHealth() > 200) {
                attackCase = 5;
            } else {
                attackCase = 9;
            }
        }
        if ((attackCase == 3 || attackCase == 6) && boss.getHealth() < boss.getMaxHealth() * .15) {
            runAttack(ChanceUtil.getRandom(OPTION_COUNT));
            return;
        }
        switch (attackCase) {
            case 1:
                ChatUtil.sendWarning(spectator, "Taste my wrath!");
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
                ChatUtil.sendWarning(spectator, "Embrace my corruption!");
                for (Player player : contained) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 20 * 12, 1));
                }
                break;
            case 3:
                ChatUtil.sendWarning(spectator, "Are you BLIND? Mwhahahaha!");
                for (Player player : contained) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 4, 0));
                }
                break;
            case 4:
                ChatUtil.sendWarning(spectator, ChatColor.DARK_RED + "Tango time!");
                activeAttacks.add(4);
                server().getScheduler().runTaskLater(inst(), () -> {
                    if (!isBossSpawned()) return;
                    Collection<Player> newContained = getContained(Player.class);
                    for (Player player : getMaster().getToolKit().removeAdmin(newContained)) {
                        if (boss.hasLineOfSight(player)) {
                            ChatUtil.send(player, "Come closer...");
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
                            ChatUtil.send(player, "Fine... No tango this time...");
                        }
                    }
                    ChatUtil.send(newContained, "Now wasn't that fun?");
                    activeAttacks.remove(4);
                }, 20 * 7);
                break;
            case 5:
                if (!damageHeals) {
                    activeAttacks.add(5);
                    ChatUtil.sendWarning(spectator, "I am everlasting!");
                    damageHeals = true;
                    server().getScheduler().runTaskLater(inst(), () -> {
                        if (damageHeals) {
                            damageHeals = false;
                            if (!isBossSpawned()) return;
                            ChatUtil.send(getContained(Player.class), "Thank you for your assistance.");
                        }
                        activeAttacks.remove(5);
                    }, 20 * 12);
                    break;
                }
                runAttack(ChanceUtil.getRandom(OPTION_COUNT));
                return;
            case 6:
                ChatUtil.sendWarning(spectator, "Fire is your friend...");
                for (Player player : contained) {
                    player.setFireTicks(20 * 30);
                }
                break;
            case 7:
                if (!damageHeals) {
                    ChatUtil.sendWarning(spectator, ChatColor.DARK_RED + "Bask in my glory!");
                    activeAttacks.add(7);
                    server().getScheduler().runTaskLater(inst(), () -> {
                        if (!isBossSpawned()) return;
                        // Set defaults
                        boolean baskInGlory = getContained(Player.class).size() == 0;
                        // Check Players
                        Collection<Player> newContained = getContained(Player.class);
                        for (Player player : getMaster().getToolKit().removeAdmin(newContained)) {
                            if (inst().hasPermission(player, "aurora.prayer.intervention") && ChanceUtil.getChance(3)) {
                                ChatUtil.send(player, "A divine wind hides you from the boss.");
                                continue;
                            }
                            if (boss.hasLineOfSight(player)) {
                                ChatUtil.sendWarning(player, ChatColor.DARK_RED + "You!");
                                baskInGlory = true;
                            }
                        }
                        //Attack
                        if (baskInGlory) {
                            damageHeals = true;
                            spawnPts.stream().filter(pt -> ChanceUtil.getChance(12)).forEach(pt -> {
                                getBukkitWorld().createExplosion(pt.getX(), pt.getY(), pt.getZ(), 10, false, false);
                            });
                            //Schedule Reset
                            server().getScheduler().runTaskLater(inst(), () -> {
                                damageHeals = false;
                            }, 10);
                            return;
                        }
                        // Notify if avoided
                        ChatUtil.send(newContained, "Gah... Afraid are you friends?");
                        activeAttacks.remove(7);
                    }, 20 * 7);
                    break;
                }
                runAttack(ChanceUtil.getRandom(OPTION_COUNT));
                break;
            case 8:
                ChatUtil.sendWarning(spectator, ChatColor.DARK_RED + "I ask thy lord for aid in this all mighty battle...");
                ChatUtil.sendWarning(spectator, ChatColor.DARK_RED + "Heed thy warning, or perish!");
                activeAttacks.add(8);
                server().getScheduler().runTaskLater(inst(), () -> {
                    if (!isBossSpawned()) return;
                    Collection<Player> newContained = getContained(Player.class);
                    ChatUtil.sendWarning(newContained, "May those who appose me die a death like no other...");
                    getMaster().getToolKit().removeAdmin(newContained).stream()
                            .filter(boss::hasLineOfSight).forEach(player -> {
                        ChatUtil.sendWarning(newContained, "Perish " + player.getName() + "!");
                        try {
                            getMaster().getPrayers().influencePlayer(
                                    player,
                                    PrayerComponent.constructPrayer(player, PrayerType.DOOM, 120000)
                            );
                        } catch (UnsupportedPrayerException e) {
                            e.printStackTrace();
                        }
                    });
                    activeAttacks.remove(8);
                }, 20 * 7);
                break;
            case 9:
                ChatUtil.send(spectator, ChatColor.DARK_RED, "My minions our time is now!");
                activeAttacks.add(9);
                IntegratedRunnable minionEater = new IntegratedRunnable() {
                    @Override
                    public boolean run(int times) {
                        if (!isBossSpawned()) return true;
                        for (LivingEntity entity : getMaster().getToolKit().removeAdmin(getContained(LivingEntity.class))) {
                            if (entity instanceof Giant || !ChanceUtil.getChance(5) || !boss.hasLineOfSight(entity)) {
                                continue;
                            }

                            double realDamage = entity.getHealth();
                            if (entity instanceof Zombie && ((Zombie) entity).isBaby()) {
                                entity.setHealth(0);
                            } else {
                                entity.damage(realDamage, boss);
                            }
                            toHeal += realDamage / 3;
                        }
                        if (TimerUtil.matchesFilter(times + 1, -1, 2)) {
                            ChatUtil.send(getContained(Player.class), ChatColor.DARK_AQUA, "The boss has drawn in: " + (int) toHeal + " health.");
                        }
                        return true;
                    }

                    @Override
                    public void end() {
                        if (!isBossSpawned()) return;
                        EntityUtil.heal(boss, toHeal);
                        toHeal = 0;
                        ChatUtil.send(getContained(Player.class), "Thank you my minions!");
                        printBossHealth();
                        activeAttacks.remove(9);
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
