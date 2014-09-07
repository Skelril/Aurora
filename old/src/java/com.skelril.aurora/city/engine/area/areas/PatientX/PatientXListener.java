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

package com.skelril.aurora.city.engine.area.areas.PatientX;

import com.sk89q.commandbook.CommandBook;
import com.sk89q.worldedit.blocks.ItemID;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.skelril.aurora.WishingWellComponent;
import com.skelril.aurora.admin.AdminState;
import com.skelril.aurora.city.engine.area.AreaListener;
import com.skelril.aurora.city.engine.area.areas.DropParty.DropPartyTask;
import com.skelril.aurora.events.PrayerApplicationEvent;
import com.skelril.aurora.events.apocalypse.ApocalypseLocalSpawnEvent;
import com.skelril.aurora.events.apocalypse.GemOfLifeUsageEvent;
import com.skelril.aurora.events.custom.item.HymnSingEvent;
import com.skelril.aurora.events.custom.item.SpecialAttackEvent;
import com.skelril.aurora.events.environment.CreepSpeakEvent;
import com.skelril.aurora.items.custom.CustomItemCenter;
import com.skelril.aurora.items.custom.CustomItems;
import com.skelril.aurora.items.specialattack.SpecialAttack;
import com.skelril.aurora.items.specialattack.attacks.hybrid.unleashed.LifeLeech;
import com.skelril.aurora.items.specialattack.attacks.melee.fear.Decimate;
import com.skelril.aurora.items.specialattack.attacks.melee.fear.SoulSmite;
import com.skelril.aurora.items.specialattack.attacks.melee.guild.rogue.Nightmare;
import com.skelril.aurora.items.specialattack.attacks.melee.unleashed.DoomBlade;
import com.skelril.aurora.items.specialattack.attacks.ranged.fear.Disarm;
import com.skelril.aurora.items.specialattack.attacks.ranged.fear.FearBomb;
import com.skelril.aurora.items.specialattack.attacks.ranged.misc.MobAttack;
import com.skelril.aurora.items.specialattack.attacks.ranged.unleashed.Famine;
import com.skelril.aurora.items.specialattack.attacks.ranged.unleashed.GlowingFog;
import com.skelril.aurora.util.ChanceUtil;
import com.skelril.aurora.util.ChatUtil;
import com.skelril.aurora.util.EntityUtil;
import com.skelril.aurora.util.LocationUtil;
import com.skelril.aurora.util.checker.RegionChecker;
import com.skelril.aurora.util.item.EffectUtil;
import com.skelril.aurora.util.item.ItemUtil;
import com.skelril.aurora.util.player.PlayerState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.bukkit.event.entity.EntityDamageEvent.DamageModifier;

public class PatientXListener extends AreaListener<PatientXArea> {
    private final CommandBook inst = CommandBook.inst();
    private final Logger log = inst.getLogger();
    private final Server server = CommandBook.server();

    public PatientXListener(PatientXArea parent) {
        super(parent);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPrayerApplication(PrayerApplicationEvent event) {

        if (parent.contains(event.getPlayer()) && event.getCause().getEffect().getType().isHoly()) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onCreepSpeak(CreepSpeakEvent event) {

        if (parent.contains(event.getPlayer()) || parent.contains(event.getTargeter())) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onApocalypseLocalSpawn(ApocalypseLocalSpawnEvent event) {
        if (parent.contains(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onHymnUse(HymnSingEvent event) {
        if (event.getHymn().equals(HymnSingEvent.Hymn.PHANTOM)) {
            Player player = event.getPlayer();
            if (LocationUtil.isInRegion(parent.getWorld(), parent.entry, player)) {
                boolean teleported;
                do {
                    teleported = player.teleport(parent.getRandomDest());
                } while (parent.boss.hasLineOfSight(player));
                if (teleported) {
                    ChatUtil.sendWarning(player, "It's been a long time since I had a worthy opponent...");
                    ChatUtil.sendWarning(player, "Let's see if you have what it takes...");
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();

        if (parent.contains(to) && !parent.contains(from)) {
            Player player = event.getPlayer();
            if (parent.admin.isAdmin(player, AdminState.ADMIN)) return;
            if (!ItemUtil.removeItemOfName(player, CustomItemCenter.build(CustomItems.PHANTOM_HYMN), 1, false)) {
                ChatUtil.sendError(player, "You need a Phantom Hymn to sacrifice to enter that area.");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (event.isFlying() && parent.contains(player) && !parent.admin.isAdmin(player)) {
            event.setCancelled(true);
            ChatUtil.send(player, "You cannot fly here!");
        }
    }

    private static Set<Class> generalBlacklistedSpecs = new HashSet<>();
    private static Set<Class> bossBlacklistedSpecs = new HashSet<>();
    private static Set<Class> ultimateBlacklistedSpecs = new HashSet<>();

    static {
        generalBlacklistedSpecs.add(Nightmare.class);
        generalBlacklistedSpecs.add(Disarm.class);
        generalBlacklistedSpecs.add(MobAttack.class);
        generalBlacklistedSpecs.add(FearBomb.class);

        bossBlacklistedSpecs.add(Famine.class);
        bossBlacklistedSpecs.add(LifeLeech.class);
        bossBlacklistedSpecs.add(SoulSmite.class);

        ultimateBlacklistedSpecs.add(GlowingFog.class);
        ultimateBlacklistedSpecs.add(Decimate.class);
        ultimateBlacklistedSpecs.add(DoomBlade.class);
    }

    private long lastUltimateAttack = 0;

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpecialAttack(SpecialAttackEvent event) {

        SpecialAttack attack = event.getSpec();

        if (!parent.contains(attack.getLocation())) return;

        Class specClass = attack.getClass();
        LivingEntity target = attack.getTarget();

        if (target != null && target.equals(parent.boss)) {
            if (bossBlacklistedSpecs.contains(specClass)) {
                event.setCancelled(true);
                return;
            }
            if (ultimateBlacklistedSpecs.contains(specClass)) {
                if (lastUltimateAttack == 0) {
                    lastUltimateAttack = System.currentTimeMillis();
                } else if (System.currentTimeMillis() - lastUltimateAttack >= 15000) {
                    lastUltimateAttack = System.currentTimeMillis();
                } else {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (generalBlacklistedSpecs.contains(specClass)) {
            event.setCancelled(true);
        }
    }

    private static Set<EntityDamageByEntityEvent.DamageCause> blockedDamage = new HashSet<>();

    static {
        blockedDamage.add(EntityDamageEvent.DamageCause.BLOCK_EXPLOSION);
        blockedDamage.add(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageEvent(EntityDamageEvent event) {

        Entity defender = event.getEntity();
        Entity attacker = null;
        Projectile projectile = null;

        if (!parent.contains(defender)) return;

        if (event instanceof EntityDamageByEntityEvent) attacker = ((EntityDamageByEntityEvent) event).getDamager();

        if (attacker instanceof Projectile) {
            if (((Projectile) attacker).getShooter() != null) {
                projectile = (Projectile) attacker;
                ProjectileSource source = projectile.getShooter();
                if (source != null && source instanceof Entity) {
                    attacker = (Entity) projectile.getShooter();
                }
            } else if (!(attacker instanceof LivingEntity)) return;
        }

        if (defender instanceof Player && blockedDamage.contains(event.getCause())) {
            // Explosive damage formula: (1 × 1 + 1) × 8 × power + 1
            // Use 49, snowball power is 3
            double ratio = event.getDamage() / 49;
            for (DamageModifier modifier : DamageModifier.values()) {
                if (event.isApplicable(modifier)) {
                    event.setDamage(modifier, 0);
                }
            }
            event.setDamage(DamageModifier.BASE, ratio * parent.difficulty);
        }

        if (defender.equals(parent.boss) && blockedDamage.contains(event.getCause())) {
            event.setCancelled(true);
            return;
        }

        if (attacker == null || !parent.contains(attacker)) return;

        if (defender instanceof Zombie) {
            if (((Zombie) defender).isBaby()) {
                return;
            }

            if (attacker instanceof Player) {
                ItemStack held = ((Player) attacker).getItemInHand();
                if (held != null && held.getTypeId() == ItemID.BLAZE_ROD) {
                    parent.modifyDifficulty(2);
                }
            }

            if (projectile != null) {
                com.sk89q.commandbook.util.entity.EntityUtil
                        .sendProjectilesFromEntity(parent.boss, 12, .5F, Snowball.class);
            }
            parent.modifyDifficulty(.5);
            parent.teleportRandom(true);
        } else if (defender instanceof Player) {
            Player player = (Player) defender;
            if (attacker.equals(parent.boss)) {
                if (inst.hasPermission(player, "aurora.prayer.intervention") && ChanceUtil.getChance(parent.difficulty)) {
                    ChatUtil.send(player, "A divine force protects you.");
                    return;
                }
                for (DamageModifier modifier : DamageModifier.values()) {
                    if (event.isApplicable(modifier)) {
                        event.setDamage(modifier, 0);
                    }
                }
                event.setDamage(DamageModifier.BASE, parent.difficulty * parent.getConfig().baseBossHit);
                return;
            }
            if (ItemUtil.hasAncientArmor(player)) {
                double diff = player.getMaxHealth() - player.getHealth();
                if (ChanceUtil.getChance(Math.max(Math.round(parent.difficulty), Math.round(player.getMaxHealth() - diff)))) {
                    EffectUtil.Ancient.powerBurst(player, event.getDamage());
                }
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile p = event.getEntity();
        if (!parent.contains(p)) return;
        if (p instanceof Snowball) {
            if (parent.boss != null && parent.boss.equals(p.getShooter())) {
                Location pt = p.getLocation();
                p.getWorld().createExplosion(pt.getX(), pt.getY(), pt.getZ(), 3, false, false);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onGemOfLifeUsage(GemOfLifeUsageEvent event) {

        if (parent.contains(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {

        if (parent.contains(event.getEntity())) {
            Entity e = event.getEntity();
            if (e instanceof Zombie) {
                event.getDrops().clear();
                if (((Zombie) e).isBaby() || parent.boss == null) {
                    if (ChanceUtil.getChance(10)) {
                        event.getDrops().add(CustomItemCenter.build(CustomItems.PHANTOM_GOLD));
                    }
                    event.setDroppedExp(20);
                    return;
                }

                Collection<Player> spectator = parent.getContained(Player.class);
                Collection<Player> contained = parent.adminKit.removeAdmin(spectator);
                ChatUtil.sendWarning(spectator, "So you think you've won? Ha!");
                ChatUtil.sendWarning(spectator, "I'll get you next time...");

                List<ItemStack> drops = new ArrayList<>();
                int playerCount = spectator.isEmpty() ? 1 : contained.size();
                int dropVal = parent.getConfig().playerVal * playerCount;
                drops.addAll(WishingWellComponent.getCalculatedLoot(Bukkit.getConsoleSender(), -1, dropVal));

                switch (ChanceUtil.getRandom(4)) {
                    case 1:
                        if (ChanceUtil.getChance(8)) {
                            drops.add(CustomItemCenter.build(CustomItems.NECROS_HELMET));
                            break;
                        }
                        drops.add(CustomItemCenter.build(CustomItems.NECTRIC_HELMET));
                        break;
                    case 2:
                        if (ChanceUtil.getChance(8)) {
                            drops.add(CustomItemCenter.build(CustomItems.NECROS_CHESTPLATE));
                            break;
                        }
                        drops.add(CustomItemCenter.build(CustomItems.NECTRIC_CHESTPLATE));
                        break;
                    case 3:
                        if (ChanceUtil.getChance(8)) {
                            drops.add(CustomItemCenter.build(CustomItems.NECROS_LEGGINGS));
                            break;
                        }
                        drops.add(CustomItemCenter.build(CustomItems.NECTRIC_LEGGINGS));
                        break;
                    case 4:
                        if (ChanceUtil.getChance(8)) {
                            drops.add(CustomItemCenter.build(CustomItems.NECROS_BOOTS));
                            break;
                        }
                        drops.add(CustomItemCenter.build(CustomItems.NECTRIC_BOOTS));
                        break;
                }

                if (ChanceUtil.getChance(100)) {
                    drops.add(CustomItemCenter.build(CustomItems.HYMN_OF_SUMMATION));
                }

                for (int i = 0; i < 8 * playerCount; ++i) {
                    drops.add(CustomItemCenter.build(CustomItems.SCROLL_OF_SUMMATION, ChanceUtil.getRandom(16)));
                }

                LocalDate date = LocalDate.now().with(Month.APRIL).withDayOfMonth(6);
                if (date.equals(LocalDate.now())) {
                    ChatUtil.send(parent.getContained(Player.class), ChatColor.GOLD, "DROPS DOUBLED!");
                    event.getDrops().addAll(event.getDrops().stream().map(ItemStack::clone).collect(Collectors.toList()));
                }

                Location target = parent.getCentralLoc();
                for (Player player : contained) {
                    player.teleport(target);
                    Vector v = new Vector(
                            ChanceUtil.getChance(2) ? 1 : -1,
                            0,
                            ChanceUtil.getChance(2) ? 1 : -1
                    );
                    if (ChanceUtil.getChance(2)) {
                        v.setX(0);
                    } else {
                        v.setZ(0);
                    }
                    player.setVelocity(v);
                }
                CuboidRegion rg = new CuboidRegion(parent.drops.getMinimumPoint(), parent.drops.getMaximumPoint());
                DropPartyTask task = new DropPartyTask(parent.getWorld(), rg, drops, new RegionChecker(rg) {
                    @Override
                    public Boolean evaluate(com.sk89q.worldedit.Vector v) {
                        Location l = new Location(parent.getWorld(), v.getX(), v.getY(), v.getZ());
                        return super.evaluate(v) && !l.getBlock().getType().isSolid();
                    }
                });
                task.setXPChance(5);
                task.setXPSize(10);
                task.start(CommandBook.inst(), server.getScheduler(), 20 * 5, 20 * 3);
                parent.freezeBlocks(100, false);

                // Reset respawn mechanics
                parent.lastDeath = System.currentTimeMillis();
                parent.boss = null;
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {

        HashMap<String, PlayerState> playerState = parent.playerState;
        Zombie boss = parent.boss;

        Player player = event.getEntity();
        if (parent.contains(player) && !parent.admin.isAdmin(player) && !playerState.containsKey(player.getName())) {
            if (parent.contains(player) && parent.isBossSpawned()) {
                EntityUtil.heal(boss, boss.getMaxHealth() / 4);
                parent.resetDifficulty();
                ChatUtil.sendWarning(parent.getContained(Player.class), "Haha, bow down "
                        + player.getName() + ", show's over for you.");
            }
            playerState.put(player.getName(), new PlayerState(player.getName(),
                    player.getInventory().getContents(),
                    player.getInventory().getArmorContents(),
                    player.getLevel(),
                    player.getExp()));
            event.getDrops().clear();

            String deathMessage;
            switch (System.currentTimeMillis() > parent.attackDur ? 0 : parent.lastAttack) {
                case 1:
                    deathMessage = " tripped over a chair";
                    break;
                case 2:
                    deathMessage = " got smashed";
                    break;
                case 3:
                    deathMessage = " bombed a performance evaluation";
                    break;
                case 4:
                    deathMessage = " became a fellow candle";
                    break;
                case 5:
                    deathMessage = " loves toxic fluids";
                    break;
                case 6:
                    deathMessage = " lost a foot or two";
                    break;
                case 7:
                    deathMessage = " went batty";
                    break;
                case 8:
                    deathMessage = " was irradiated";
                    break;
                case 9:
                    deathMessage = " took a snowball to the face";
                    break;
                default:
                    deathMessage = " froze";
                    break;
            }

            event.setDeathMessage(player.getName() + deathMessage);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {

        HashMap<String, PlayerState> playerState = parent.playerState;

        Player player = event.getPlayer();
        // Restore their inventory if they have one stored
        if (playerState.containsKey(player.getName()) && !parent.admin.isAdmin(player)) {

            try {
                PlayerState identity = playerState.get(player.getName());

                // Restore the contents
                player.getInventory().setArmorContents(identity.getArmourContents());
                player.getInventory().setContents(identity.getInventoryContents());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                playerState.remove(player.getName());
            }
        }
    }
}
