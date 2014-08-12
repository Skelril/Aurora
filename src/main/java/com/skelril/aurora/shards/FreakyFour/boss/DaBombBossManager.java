/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shards.FreakyFour.boss;

import com.skelril.OpenBoss.BossManager;
import com.skelril.OpenBoss.instruction.processor.BindProcessor;
import com.skelril.OpenBoss.instruction.processor.DamagedProcessor;
import com.skelril.OpenBoss.instruction.processor.UnbindProcessor;
import com.skelril.aurora.bosses.instruction.HealthPrint;
import com.skelril.aurora.bosses.instruction.SHBindInstruction;
import com.skelril.aurora.shards.FreakyFour.FreakyFourBoss;
import com.skelril.aurora.shards.FreakyFour.FreakyFourConfig;
import com.skelril.aurora.shards.FreakyFour.FreakyFourInstance;
import com.skelril.aurora.shards.FreakyFour.boss.instruction.BackTeleportInstruction;
import com.skelril.aurora.shards.FreakyFour.boss.instruction.HealableInstruction;
import org.bukkit.entity.Player;

import static com.skelril.aurora.shards.FreakyFour.FreakyFourInstance.getInst;

public class DaBombBossManager extends BossManager {

    private FreakyFourConfig config;

    public DaBombBossManager(FreakyFourConfig config) {
        this.config = config;
        handleBinds();
        handleUnbinds();
        handleDamage();
        handleDamaged();
    }

    private void handleBinds() {
        BindProcessor bindProcessor = getBindProcessor();
        bindProcessor.addInstruction(new SHBindInstruction("Da Bomb", config.daBombHP));
    }

    private void handleUnbinds() {
        UnbindProcessor unbindProcessor = getUnbindProcessor();
        unbindProcessor.addInstruction(condition -> {
            FreakyFourInstance inst = getInst(condition.getBoss().getDetail());
            if (inst == null) return null;
            inst.bossDied(inst.getCurrentboss());
            inst.setCurrentboss(FreakyFourBoss.SNIPEE);
            Player player = condition.getBoss().getEntity().getKiller();
            if (player != null) {
                player.teleport(inst.getCenter(inst.getCurrentboss()));
            }
            inst.spawnBoss(inst.getCurrentboss());
            return null;
        });
    }

    private void handleDamage() {

    }

    private void handleDamaged() {
        DamagedProcessor damagedProcessor = getDamagedProcessor();
        damagedProcessor.addInstruction(new BackTeleportInstruction(config));
        damagedProcessor.addInstruction(new HealableInstruction());
        damagedProcessor.addInstruction(new HealthPrint());
    }
}
