/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shard.instance.Catacombs;

import com.skelril.aurora.events.PlayerInstanceDeathEvent;
import com.skelril.aurora.events.PrayerApplicationEvent;
import com.skelril.aurora.events.custom.item.ChanceActivationEvent;
import com.skelril.aurora.events.environment.CreepSpeakEvent;
import com.skelril.aurora.events.shard.PartyActivateEvent;
import com.skelril.aurora.shard.instance.ShardListener;
import com.skelril.aurora.util.ChanceUtil;
import com.skelril.aurora.util.ChatUtil;
import com.skelril.aurora.util.KeepAction;
import com.skelril.aurora.util.player.PlayerRespawnProfile_1_7_10;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

public class CatacombsListener extends ShardListener<Catacombs> {
    public CatacombsListener(Catacombs shard) {
        super(shard);
    }

    @EventHandler
    public void onPartyActivate(PartyActivateEvent event) {
        if (!event.hasInstance() && shard.matchesShard(event.getShard())) {
            CatacombsInstance instance = shard.makeInstance();
            instance.teleportTo(shard.wrapPlayers(event.getPlayers()));
            event.setInstance(instance);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPrayerApplication(PrayerApplicationEvent event) {
        CatacombsInstance inst = shard.getInstance(event.getPlayer().getLocation());
        if (inst != null && event.getCause().getEffect().getType().isHoly()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCreepSpeak(CreepSpeakEvent event) {
        CatacombsInstance inst = shard.getInstance(event.getPlayer().getLocation());
        if (inst != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        CatacombsInstance inst = shard.getInstance(player.getLocation());
        if (inst != null && event.isFlying() && !inst.getMaster().getAdmin().isAdmin(player)) {
            event.setCancelled(true);
            ChatUtil.send(player, "You cannot fly here!");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onChanceActivation(ChanceActivationEvent event) {
        CatacombsInstance inst = shard.getInstance(event.getWhere());
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
    public void onPlayerInstDeath(PlayerInstanceDeathEvent event) {
        CatacombsInstance inst = shard.getInstance(event.getPlayer());
        if (inst == null) return;
        PlayerRespawnProfile_1_7_10 profile = event.getProfile();
        profile.setLevelAction(KeepAction.DESTROY);
        profile.setExperienceAction(KeepAction.DESTROY);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        CatacombsInstance inst = shard.getInstance(event.getEntity());
        if (inst == null) return;
        event.setDroppedExp(25);
        event.getDrops().clear();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        CatacombsInstance inst = shard.getInstance(player.getLocation());
        if (inst != null) {
            event.getDrops().clear();
            String deathMessage;
            switch (ChanceUtil.getRandom(7)) {
                case 1:
                    deathMessage = " is now one with the catacombs";
                    break;
                case 2:
                    deathMessage = " stumbled on some bones";
                    break;
                case 3:
                    deathMessage = " joined the undead army";
                    break;
                case 4:
                    deathMessage = " is now at the mercy of the Necromancers";
                    break;
                case 5:
                    deathMessage = " joined their ancestors";
                    break;
                case 6:
                    deathMessage = " now craves human flesh";
                    break;
                default:
                    deathMessage = " didn't make it out with all their brain";
                    break;
            }
            event.setDeathMessage(player.getName() + deathMessage);
        }
    }
}
