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
import com.skelril.OpenBoss.condition.DamagedCondition;
import com.skelril.OpenBoss.instruction.DamagedInstruction;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public abstract class DamageNearby implements DamagedInstruction {

    private final InstructionResult<DamagedInstruction> next;

    public DamageNearby() {
        this(null);
    }

    public DamageNearby(InstructionResult<DamagedInstruction> next) {
        this.next = next;
    }

    public boolean checkTarget(Boss boss, LivingEntity entity) {
        return true;
    }

    public boolean isLivingEntity(Entity entity) {
        return entity instanceof LivingEntity;
    }

    public abstract double getDamage(EntityDetail detail);

    public void damage(Boss boss, LivingEntity entity) {
        entity.damage(getDamage(boss.getDetail()), boss.getEntity());
    }

    @Override
    public InstructionResult<DamagedInstruction> process(DamagedCondition condition) {
        Boss boss = condition.getBoss();
        LivingEntity bossEntity = boss.getEntity();
        bossEntity.getNearbyEntities(2, 2, 2).stream()
                .filter(this::isLivingEntity)
                .filter(e -> checkTarget(boss, (LivingEntity) e))
                .forEach(entity -> damage(boss, (LivingEntity) entity));
        return next;
    }
}
