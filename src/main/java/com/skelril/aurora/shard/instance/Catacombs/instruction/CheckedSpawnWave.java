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

package com.skelril.aurora.shard.instance.Catacombs.instruction;

import com.skelril.OpenBoss.InstructionResult;
import com.skelril.OpenBoss.condition.UnbindCondition;
import com.skelril.OpenBoss.instruction.UnbindInstruction;

import static com.sk89q.commandbook.CommandBook.inst;
import static com.skelril.aurora.shard.instance.Catacombs.CatacombsInstance.getInst;
import static com.zachsthings.libcomponents.bukkit.BasePlugin.server;

public class CheckedSpawnWave implements UnbindInstruction {

    private final InstructionResult<UnbindInstruction> next;

    public CheckedSpawnWave() {
        this(null);
    }

    public CheckedSpawnWave(InstructionResult<UnbindInstruction> next) {
        this.next = next;
    }

    @Override
    public InstructionResult<UnbindInstruction> process(UnbindCondition condition) {
        server().getScheduler().runTaskLater(inst(), () -> {
            getInst(condition.getBoss().getDetail()).checkedSpawnWave();
        }, 1);
        return next;
    }
}
