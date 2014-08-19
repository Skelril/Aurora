/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shard.instance.FreakyFour.boss;

import com.skelril.OpenBoss.BossManager;
import com.skelril.OpenBoss.instruction.processor.BindProcessor;
import com.skelril.OpenBoss.instruction.processor.DamageProcessor;
import com.skelril.OpenBoss.instruction.processor.DamagedProcessor;
import com.skelril.OpenBoss.instruction.processor.UnbindProcessor;
import com.skelril.aurora.combat.bosses.instruction.HealthPrint;
import com.skelril.aurora.combat.bosses.instruction.SHBindInstruction;
import com.skelril.aurora.shard.instance.FreakyFour.FreakyFourBoss;
import com.skelril.aurora.shard.instance.FreakyFour.FreakyFourConfig;
import com.skelril.aurora.shard.instance.FreakyFour.FreakyFourInstance;
import com.skelril.aurora.util.ChanceUtil;
import com.skelril.aurora.util.ChatUtil;
import com.skelril.aurora.util.EntityUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Collection;

import static com.skelril.aurora.shard.instance.FreakyFour.FreakyFourInstance.getInst;

public class FrimusBossManager extends BossManager {

    private FreakyFourConfig config;

    public FrimusBossManager(FreakyFourConfig config) {
        this.config = config;
        handleBinds();
        handleUnbinds();
        handleDamage();
        handleDamaged();
    }

    private void handleBinds() {
        BindProcessor bindProcessor = getBindProcessor();
        bindProcessor.addInstruction(new SHBindInstruction("Frimus", config.frimusHP));
    }

    private void handleUnbinds() {
        UnbindProcessor unbindProcessor = getUnbindProcessor();
        unbindProcessor.addInstruction(condition -> {
            FreakyFourInstance inst = getInst(condition.getBoss().getDetail());
            if (inst == null) return null;
            inst.bossDied(inst.getCurrentboss());
            inst.setCurrentboss(FreakyFourBoss.DA_BOMB);
            Player player = condition.getBoss().getEntity().getKiller();
            if (player != null) {
                player.teleport(inst.getCenter(inst.getCurrentboss()));
            }
            inst.spawnBoss(inst.getCurrentboss());
            return null;
        });
    }

    private void handleDamage() {
        DamageProcessor damageProcessor = getDamageProcessor();
        damageProcessor.addInstruction(condition -> {
            Entity attacked = condition.getAttacked();
            if (attacked instanceof LivingEntity) {
                EntityUtil.forceDamage(
                        attacked,
                        Math.max(
                                1,
                                ChanceUtil.getRandom(((LivingEntity) attacked).getHealth()) - 5
                        )
                );
            }
            return null;
        });
    }

    private void handleDamaged() {
        DamagedProcessor damagedProcessor = getDamagedProcessor();
        damagedProcessor.addInstruction(new HealthPrint());
        damagedProcessor.addInstruction(condition -> {
            EntityDamageEvent event = condition.getEvent();
            if (event.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE)) {
                FreakyFourInstance inst = getInst(condition.getBoss().getDetail());
                Collection<Player> players = inst.getContained(inst.getRegion(FreakyFourBoss.FRIMUS), Player.class);
                ChatUtil.sendNotice(players, "Projectiles can't harm me... Mwahahaha!");
                event.setCancelled(true);
            }
            return null;
        });
    }
}
