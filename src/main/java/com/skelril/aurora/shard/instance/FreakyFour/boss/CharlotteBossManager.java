/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shard.instance.FreakyFour.boss;

import com.skelril.OpenBoss.BossListener;
import com.skelril.OpenBoss.BossManager;
import com.skelril.OpenBoss.instruction.processor.BindProcessor;
import com.skelril.OpenBoss.instruction.processor.DamagedProcessor;
import com.skelril.OpenBoss.instruction.processor.UnbindProcessor;
import com.skelril.aurora.combat.bosses.instruction.HealthPrint;
import com.skelril.aurora.combat.bosses.instruction.SHBindInstruction;
import com.skelril.aurora.shard.instance.FreakyFour.FreakyFourBoss;
import com.skelril.aurora.shard.instance.FreakyFour.FreakyFourConfig;
import com.skelril.aurora.shard.instance.FreakyFour.FreakyFourInstance;
import org.bukkit.entity.Player;

import static com.sk89q.commandbook.CommandBook.registerEvents;
import static com.skelril.aurora.shard.instance.FreakyFour.FreakyFourInstance.getInst;

public class CharlotteBossManager extends BossManager {

    private CharlotteMinionManager minionManager = new CharlotteMinionManager();
    private FreakyFourConfig config;

    public CharlotteBossManager(FreakyFourConfig config) {
        this.config = config;
        handleBinds();
        handleUnbinds();
        handleDamaged();

        registerEvents(new BossListener(minionManager));
    }

    public CharlotteMinionManager getMinionManager() {
        return minionManager;
    }

    private void handleBinds() {
        BindProcessor bindProcessor = getBindProcessor();
        bindProcessor.addInstruction(new SHBindInstruction("Charlotte", config.charlotteHP));
    }

    private void handleUnbinds() {
        UnbindProcessor unbindProcessor = getUnbindProcessor();
        unbindProcessor.addInstruction(condition -> {
            FreakyFourInstance inst = getInst(condition.getBoss().getDetail());
            if (inst == null) return null;
            inst.bossDied(inst.getCurrentboss());
            inst.setCurrentboss(FreakyFourBoss.FRIMUS);
            Player player = condition.getBoss().getEntity().getKiller();
            if (player != null) {
                player.teleport(inst.getCenter(inst.getCurrentboss()));
            }
            inst.spawnBoss(inst.getCurrentboss());
            return null;
        });
    }

    private void handleDamaged() {
        DamagedProcessor damagedProcessor = getDamagedProcessor();
        damagedProcessor.addInstruction(new HealthPrint());
    }
}
