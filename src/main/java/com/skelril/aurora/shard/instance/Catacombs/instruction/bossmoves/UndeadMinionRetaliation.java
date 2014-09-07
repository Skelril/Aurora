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

package com.skelril.aurora.shard.instance.Catacombs.instruction.bossmoves;

import com.skelril.OpenBoss.Boss;
import com.skelril.OpenBoss.EntityDetail;
import com.skelril.OpenBoss.InstructionResult;
import com.skelril.OpenBoss.condition.DamagedCondition;
import com.skelril.OpenBoss.instruction.DamagedInstruction;
import com.skelril.aurora.shard.instance.Catacombs.CatacombEntityDetail;
import com.skelril.aurora.shard.instance.Catacombs.CatacombsInstance;
import com.skelril.aurora.util.ChanceUtil;

import static com.skelril.aurora.shard.instance.Catacombs.CatacombsInstance.getInst;

public class UndeadMinionRetaliation implements DamagedInstruction {

    private final InstructionResult<DamagedInstruction> next;
    private final int baseActivation;

    public UndeadMinionRetaliation() {
        this(25);
    }

    public UndeadMinionRetaliation(int baseActivation) {
        this(null, baseActivation);
    }

    public UndeadMinionRetaliation(InstructionResult<DamagedInstruction> next, int baseActivation) {
        this.next = next;
        this.baseActivation = baseActivation;
    }

    public boolean activate(EntityDetail detail) {
        return ChanceUtil.getChance(baseActivation - CatacombEntityDetail.getFrom(detail).getWave());
    }

    public int quantity(EntityDetail detail) {
        return activate(detail) ? ChanceUtil.getRangedRandom(3, 17) : 1;
    }

    @Override
    public InstructionResult<DamagedInstruction> process(DamagedCondition condition) {
        Boss boss = condition.getBoss();
        CatacombsInstance instance = getInst(boss.getDetail());
        if (activate(boss.getDetail())) {
            for (int i = quantity(boss.getDetail()); i > 0; --i){
                instance.spawnWaveMob(boss.getEntity().getLocation());
            }
        }
        return next;
    }
}
