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

import com.skelril.OpenBoss.BossManager;
import com.skelril.OpenBoss.instruction.processor.DamageProcessor;
import com.skelril.OpenBoss.instruction.processor.DamagedProcessor;
import com.skelril.aurora.combat.bosses.instruction.HealthPrint;
import com.skelril.aurora.shard.instance.FreakyFour.FreakyFourBoss;
import com.skelril.aurora.shard.instance.FreakyFour.FreakyFourInstance;
import com.skelril.aurora.util.EntityUtil;
import org.bukkit.entity.LivingEntity;

import static com.skelril.aurora.shard.instance.FreakyFour.FreakyFourInstance.getInst;

public class CharlotteMinionManager extends BossManager {

    public CharlotteMinionManager() {
        handleDamage();
        handleDamaged();
    }

    private void handleDamage() {
        DamageProcessor damageProcessor = getDamageProcessor();
        damageProcessor.addInstruction(condition -> {
            FreakyFourInstance inst = getInst(condition.getBoss().getDetail());
            if (inst == null) return null;
            LivingEntity boss = inst.getBoss(FreakyFourBoss.CHARLOTTE);
            EntityUtil.heal(boss, condition.getEvent().getDamage());
            return null;
        });
    }

    private void handleDamaged() {
        DamagedProcessor damagedProcessor = getDamagedProcessor();
        damagedProcessor.addInstruction(new HealthPrint());
    }
}
