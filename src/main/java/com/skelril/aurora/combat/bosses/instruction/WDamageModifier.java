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

package com.skelril.aurora.combat.bosses.instruction;

import com.skelril.OpenBoss.Boss;
import com.skelril.OpenBoss.EntityDetail;
import com.skelril.OpenBoss.InstructionResult;
import com.skelril.OpenBoss.condition.DamageCondition;
import com.skelril.OpenBoss.instruction.DamageInstruction;
import com.skelril.aurora.combat.bosses.detail.WBossDetail;
import com.skelril.aurora.util.ChanceUtil;
import org.bukkit.event.entity.EntityDamageEvent;

public class WDamageModifier implements DamageInstruction {

    private final InstructionResult<DamageInstruction> next;

    public WDamageModifier() {
        this(null);
    }

    public WDamageModifier(InstructionResult<DamageInstruction> next) {
        this.next = next;
    }

    @Override
    public InstructionResult<DamageInstruction> process(DamageCondition condition) {
        EntityDamageEvent event = condition.getEvent();
        if (event == null) return null;
        double origDmg = event.getOriginalDamage(EntityDamageEvent.DamageModifier.BASE);
        int level = getWLevel(condition.getBoss());
        int addDmg = ChanceUtil.getRandom(ChanceUtil.getRandom(level)) - 1;
        event.setDamage(EntityDamageEvent.DamageModifier.BASE, origDmg + addDmg);
        return next;
    }

    private int getWLevel(Boss boss) {
        EntityDetail detail = boss.getDetail();
        if (detail instanceof WBossDetail) {
            return ((WBossDetail) detail).getLevel();
        }
        return 1;
    }
}
