/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shard.instance.ShnugglesPrime;

import com.sk89q.worldedit.blocks.ItemID;
import com.skelril.aurora.events.PlayerInstanceDeathEvent;
import com.skelril.aurora.events.PrayerApplicationEvent;
import com.skelril.aurora.events.custom.item.ChanceActivationEvent;
import com.skelril.aurora.events.custom.item.SpecialAttackEvent;
import com.skelril.aurora.events.environment.CreepSpeakEvent;
import com.skelril.aurora.events.shard.PartyActivateEvent;
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
import org.bukkit.entity.Giant;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class ShnugglesPrimeListener extends ShardListener<ShnugglesPrime> {
    public ShnugglesPrimeListener(ShnugglesPrime shard) {
        super(shard);
    }

    @EventHandler
    public void onPartyActivate(PartyActivateEvent event) {
        if (!event.hasInstance() && shard.matchesShard(event.getShard())) {
            ShnugglesPrimeInstance instance = shard.makeInstance();
            instance.teleportTo(shard.wrapPlayers(event.getPlayers()));
            event.setInstance(instance);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        ShnugglesPrimeInstance inst = shard.getInstance(player.getLocation());
        if (inst != null && event.isFlying() && !inst.getMaster().getAdmin().isAdmin(player)) {
            event.setCancelled(true);
            ChatUtil.sendNotice(player, "You cannot fly here!");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPrayerApplication(PrayerApplicationEvent event) {
        ShnugglesPrimeInstance inst = shard.getInstance(event.getPlayer().getLocation());
        if (inst != null && event.getCause().getEffect().getType().isHoly()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCreepSpeak(CreepSpeakEvent event) {
        ShnugglesPrimeInstance inst = shard.getInstance(event.getPlayer().getLocation());
        if (inst != null) {
            event.setCancelled(true);
        }
    }

    private static Set<Class> generalBlacklistedSpecs = new HashSet<>();
    private static Set<Class> bossBlacklistedSpecs = new HashSet<>();
    private static Set<Class> ultimateBlacklistedSpecs = new HashSet<>();

    static {
        generalBlacklistedSpecs.add(GlowingFog.class);
        generalBlacklistedSpecs.add(Nightmare.class);
        generalBlacklistedSpecs.add(Disarm.class);
        generalBlacklistedSpecs.add(MobAttack.class);
        generalBlacklistedSpecs.add(FearBomb.class);

        bossBlacklistedSpecs.add(Famine.class);
        bossBlacklistedSpecs.add(LifeLeech.class);
        bossBlacklistedSpecs.add(SoulSmite.class);

        ultimateBlacklistedSpecs.add(Decimate.class);
        ultimateBlacklistedSpecs.add(DoomBlade.class);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpecialAttack(SpecialAttackEvent event) {
        SpecialAttack attack = event.getSpec();
        ShnugglesPrimeInstance inst = shard.getInstance(attack.getLocation());
        if (inst == null) return;
        Class specClass = attack.getClass();
        LivingEntity target = attack.getTarget();
        if (target != null && target instanceof Giant) {
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

    @EventHandler(ignoreCancelled = true)
    public void onChanceActivation(ChanceActivationEvent event) {
        ShnugglesPrimeInstance inst = shard.getInstance(event.getWhere());
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
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        ShnugglesPrimeInstance inst = shard.getInstance(entity);
        if (inst == null) return;
        event.getDrops().clear();
        if (entity instanceof Zombie && ((Zombie) entity).isBaby()) {
            if (ChanceUtil.getChance(28)) {
                event.getDrops().add(new ItemStack(ItemID.GOLD_NUGGET, ChanceUtil.getRandom(3)));
            }
            event.setDroppedExp(14);
        }
    }

    @EventHandler
    public void onPlayerInstDeath(PlayerInstanceDeathEvent event) {
        ShnugglesPrimeInstance inst = shard.getInstance(event.getPlayer());
        if (inst == null) return;
        PlayerRespawnProfile_1_7_10 profile = event.getProfile();
        profile.setLevelAction(KeepAction.DESTROY);
        profile.setExperienceAction(KeepAction.DESTROY);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        ShnugglesPrimeInstance inst = shard.getInstance(player.getLocation());
        if (inst != null) {
            inst.healBoss(.33F);
            event.getDrops().clear();
            String deathMessage;
            switch (inst.getLastAttack()) {
                case 1:
                    deathMessage = " discovered how tasty the boss's wrath is";
                    break;
                case 2:
                    deathMessage = " embraced the boss's corruption";
                    break;
                case 3:
                    deathMessage = " did not die seeing";
                    break;
                case 4:
                    deathMessage = " found out the boss has two left feet";
                    break;
                case 5:
                    deathMessage = " needs not pester invincible overlords";
                    break;
                case 6:
                    deathMessage = " died to a terrible inferno";
                    break;
                case 7:
                    deathMessage = " basked in the glory of the boss";
                    break;
                case 8:
                    deathMessage = " was the victim of a devastating prayer";
                    break;
                case 9:
                    deathMessage = " has been consumed by the boss";
                    break;
                default:
                    deathMessage = " died while attempting to slay the boss";
                    break;
            }
            event.setDeathMessage(player.getName() + deathMessage);
        }
    }
}
