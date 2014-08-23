/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shard.instance.PatientX;

import com.skelril.aurora.events.PlayerInstanceDeathEvent;
import com.skelril.aurora.events.PrayerApplicationEvent;
import com.skelril.aurora.events.apocalypse.ApocalypseLocalSpawnEvent;
import com.skelril.aurora.events.custom.item.ChanceActivationEvent;
import com.skelril.aurora.events.custom.item.SpecialAttackEvent;
import com.skelril.aurora.events.environment.CreepSpeakEvent;
import com.skelril.aurora.events.shard.PartyActivateEvent;
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
import com.skelril.aurora.shard.instance.ShardListener;
import com.skelril.aurora.util.ChanceUtil;
import com.skelril.aurora.util.ChatUtil;
import com.skelril.aurora.util.KeepAction;
import com.skelril.aurora.util.player.PlayerRespawnProfile_1_7_10;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PatientXListener extends ShardListener<PatientX> {
    public PatientXListener(PatientX shard) {
        super(shard);
    }

    @EventHandler
    public void onPartyActivate(PartyActivateEvent event) {
        if (!event.hasInstance() && shard.matchesShard(event.getShard())) {
            PatientXInstance instance = shard.makeInstance();
            instance.teleportTo(shard.wrapPlayers(event.getPlayers()));
            event.setInstance(instance);

            List<Player> players = event.getPlayers();
            ChatUtil.sendWarning(players, "It's been a long time since I had a worthy opponent...");
            ChatUtil.sendWarning(players, "Let's see if you have what it takes...");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPrayerApplication(PrayerApplicationEvent event) {
        PatientXInstance inst = shard.getInstance(event.getPlayer().getLocation());
        if (inst != null && event.getCause().getEffect().getType().isHoly()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCreepSpeak(CreepSpeakEvent event) {
        PatientXInstance inst = shard.getInstance(event.getPlayer().getLocation());
        if (inst != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onApocalypseLocalSpawn(ApocalypseLocalSpawnEvent event) {
        PatientXInstance inst = shard.getInstance(event.getPlayer().getLocation());
        if (inst != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        PatientXInstance inst = shard.getInstance(player.getLocation());
        if (inst != null && event.isFlying() && !inst.getMaster().getAdmin().isAdmin(player)) {
            event.setCancelled(true);
            ChatUtil.sendNotice(player, "You cannot fly here!");
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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpecialAttack(SpecialAttackEvent event) {
        SpecialAttack attack = event.getSpec();
        PatientXInstance inst = shard.getInstance(attack.getLocation());
        if (inst == null) return;
        Class specClass = attack.getClass();
        LivingEntity target = attack.getTarget();
        if (target != null && target.equals(inst.getBoss())) {
            if (bossBlacklistedSpecs.contains(specClass)) {
                event.setCancelled(true);
                return;
            }
            if (ultimateBlacklistedSpecs.contains(specClass)) {
                if (inst.canUseUltimate(15000)) {
                    inst.updateLastUltimate();
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



    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageEvent(EntityDamageEvent event) {
        Entity defender = event.getEntity();

        PatientXInstance inst = shard.getInstance(defender);
        if (inst == null) return;

        if (defender instanceof Player && inst.getMaster().getBlockedDamage().contains(event.getCause())) {
            // Explosive damage formula: (1 × 1 + 1) × 8 × power + 1
            // Use 49, snowball power is 3
            double ratio = event.getDamage() / 49;
            for (EntityDamageEvent.DamageModifier modifier : EntityDamageEvent.DamageModifier.values()) {
                if (event.isApplicable(modifier)) {
                    event.setDamage(modifier, 0);
                }
            }
            event.setDamage(EntityDamageEvent.DamageModifier.BASE, ratio * inst.getDifficulty());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onChanceActivation(ChanceActivationEvent event) {
        PatientXInstance inst = shard.getInstance(event.getWhere());
        if (inst == null) return;
        Player player = event.getPlayer();
        switch (event.getType()) {
            case WEAPON:
                event.increaseChance(10);
                break;
            case ARMOR:
                double diff = player.getMaxHealth() - player.getHealth();
                event.increaseChance((int) ((diff / 2) + 5));
                break;
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile p = event.getEntity();
        PatientXInstance inst = shard.getInstance(p);
        if (inst == null) return;
        if (p instanceof Snowball) {
            if (p.getShooter() != null && p.getShooter().equals(inst.getBoss())) {
                Location pt = p.getLocation();
                p.getWorld().createExplosion(pt.getX(), pt.getY(), pt.getZ(), 3, false, false);
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        PatientXInstance inst = shard.getInstance(entity);
        if (inst == null) return;
        event.getDrops().clear();
        if (entity instanceof Zombie && ((Zombie) entity).isBaby()) {
            if (ChanceUtil.getChance(10)) {
                event.getDrops().add(CustomItemCenter.build(CustomItems.PHANTOM_GOLD));
            }
            event.setDroppedExp(20);
        }
    }

    @EventHandler
    public void onPlayerInstDeath(PlayerInstanceDeathEvent event) {
        PatientXInstance inst = shard.getInstance(event.getPlayer());
        if (inst == null) return;
        PlayerRespawnProfile_1_7_10 profile = event.getProfile();
        profile.setLevelAction(KeepAction.DESTROY);
        profile.setExperienceAction(KeepAction.DESTROY);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {

        PatientXInstance inst = shard.getInstance(event.getEntity());
        if (inst == null) return;

        Player player = event.getEntity();
        if (inst.isBossSpawned()) {
            inst.healBoss(.25F);
            inst.resetDifficulty();
            ChatUtil.sendWarning(inst.getContained(Player.class), "Haha, bow down "
                    + player.getName() + ", show's over for you.");
        }

        String deathMessage;
        switch (inst.getLastAttack()) {
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
