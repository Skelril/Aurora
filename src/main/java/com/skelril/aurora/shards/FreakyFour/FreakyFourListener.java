/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shards.FreakyFour;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.skelril.OpenBoss.Boss;
import com.skelril.aurora.events.PlayerInstanceDeathEvent;
import com.skelril.aurora.events.shard.PartyActivateEvent;
import com.skelril.aurora.shards.ShardListener;
import com.skelril.aurora.util.ChanceUtil;
import com.skelril.aurora.util.ChatUtil;
import com.skelril.aurora.util.KeepAction;
import com.skelril.aurora.util.player.PlayerRespawnProfile_1_7_10;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;

import java.util.List;

public class FreakyFourListener extends ShardListener<FreakyFour> {
    public FreakyFourListener(FreakyFour shard) {
        super(shard);
    }

    @EventHandler
    public void onPartyActivate(PartyActivateEvent event) {
        if (shard.matchesShard(event.getShard())) {
            FreakyFourInstance instance = shard.makeInstance();
            instance.teleportTo(shard.wrapPlayers(event.getPlayers()));
            List<Player> players = event.getPlayers();
            ChatUtil.sendWarning(players, "You think you can beat us? Ha! we'll see about that...");
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        FreakyFourInstance inst = shard.getInstance(entity);
        if (inst == null) return;
        event.getDrops().clear();
    }

    @EventHandler
    public void onPlayerInstDeath(PlayerInstanceDeathEvent event) {
        FreakyFourInstance inst = shard.getInstance(event.getPlayer());
        if (inst == null) return;
        PlayerRespawnProfile_1_7_10 profile = event.getProfile();
        profile.setDroppedExp(0);
        profile.setLevelAction(KeepAction.DESTROY);
        profile.setExperienceAction(KeepAction.DESTROY);
    }

    @EventHandler(ignoreCancelled = true)
    public void onCreeperExplode(ExplosionPrimeEvent event) {
        Entity entity = event.getEntity();
        FreakyFourInstance inst = shard.getInstance(entity);
        if (inst != null) {
            Boss boss = inst.getMaster().getManager(FreakyFourBoss.DA_BOMB).lookup(entity.getUniqueId());
            if (boss == null) return;
            CuboidRegion dabomb_RG = inst.getRegion(FreakyFourBoss.DA_BOMB);
            Vector min = dabomb_RG.getMinimumPoint();
            Vector max = dabomb_RG.getMaximumPoint();
            int minX = min.getBlockX();
            int minY = min.getBlockY();
            int minZ = min.getBlockZ();
            int maxX = max.getBlockX();
            int maxZ = max.getBlockZ();
            int dmgFact = (int) (Math.max(3, (((Creeper) entity).getHealth() / ((Creeper) entity).getMaxHealth())
                    * inst.getMaster().getConfig().daBombTNTStrength));
            for (int x = minX; x < maxX; ++x) {
                for (int z = minZ; z < maxZ; ++z) {
                    if (ChanceUtil.getChance(inst.getMaster().getConfig().daBombTNT)) {
                        inst.getBukkitWorld().createExplosion(x, minY, z, dmgFact, false, false);
                    }
                }
            }
            throwBack(entity);
            event.setCancelled(true);
        }
    }

    private void throwBack(Entity entity) {
        org.bukkit.util.Vector vel = entity.getLocation().getDirection();
        vel.multiply(-ChanceUtil.getRangedRandom(1.2, 1.5));
        vel.setY(Math.min(.8, Math.max(.175, vel.getY())));
        entity.setVelocity(vel);
    }
}
