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

package com.skelril.aurora.shard.instance.FreakyFour.boss;

import com.skelril.OpenBoss.BossListener;
import com.skelril.OpenBoss.BossManager;
import com.skelril.OpenBoss.instruction.processor.BindProcessor;
import com.skelril.OpenBoss.instruction.processor.DamagedProcessor;
import com.skelril.OpenBoss.instruction.processor.UnbindProcessor;
import com.skelril.aurora.combat.bosses.instruction.HealthPrint;
import com.skelril.aurora.combat.bosses.instruction.SHBindInstruction;
import com.skelril.aurora.shard.instance.FreakyFour.FreakyFourConfig;
import com.skelril.aurora.shard.instance.FreakyFour.FreakyFourInstance;

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
            return null;
        });
    }

    private void handleDamaged() {
        DamagedProcessor damagedProcessor = getDamagedProcessor();
        damagedProcessor.addInstruction(new HealthPrint());
    }
}
