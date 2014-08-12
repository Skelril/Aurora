/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shards.FreakyFour.boss;

import com.skelril.OpenBoss.BossManager;
import com.skelril.OpenBoss.instruction.processor.BindProcessor;
import com.skelril.OpenBoss.instruction.processor.DamageProcessor;
import com.skelril.OpenBoss.instruction.processor.DamagedProcessor;
import com.skelril.OpenBoss.instruction.processor.UnbindProcessor;
import com.skelril.aurora.bosses.instruction.HealthPrint;
import com.skelril.aurora.bosses.instruction.SHBindInstruction;
import com.skelril.aurora.shards.FreakyFour.FreakyFourBoss;
import com.skelril.aurora.shards.FreakyFour.FreakyFourConfig;
import com.skelril.aurora.shards.FreakyFour.FreakyFourInstance;
import org.bukkit.entity.Player;

import static com.skelril.aurora.shards.FreakyFour.FreakyFourInstance.getInst;

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

    }

    private void handleDamaged() {
        DamagedProcessor damagedProcessor = getDamagedProcessor();
        damagedProcessor.addInstruction(new HealthPrint());
    }
}
