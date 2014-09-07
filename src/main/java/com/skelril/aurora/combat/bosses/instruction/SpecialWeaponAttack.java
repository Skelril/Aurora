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
import com.skelril.OpenBoss.InstructionResult;
import com.skelril.OpenBoss.condition.DamageCondition;
import com.skelril.OpenBoss.instruction.DamageInstruction;
import com.skelril.aurora.items.generic.weapons.SpecWeaponImpl;
import com.skelril.aurora.items.specialattack.SpecialAttack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class SpecialWeaponAttack implements DamageInstruction {

    private final InstructionResult<DamageInstruction> next;
    private final SpecWeaponImpl weapon;

    public SpecialWeaponAttack(SpecWeaponImpl weapon) {
        this(null, weapon);
    }

    public SpecialWeaponAttack(InstructionResult<DamageInstruction> next, SpecWeaponImpl weapon) {
        this.next = next;
        this.weapon = weapon;
    }

    public SpecWeaponImpl getWeapon() {
        return weapon;
    }

    public SpecialAttack getSpec(Boss owner, LivingEntity target) {
        return weapon.getSpecial(owner.getEntity(), target);
    }

    public void activateSpecial(SpecialAttack spec) {
        spec.activate();
    }

    @Override
    public InstructionResult<DamageInstruction> process(DamageCondition condition) {
        Boss boss = condition.getBoss();
        Entity eToHit = condition.getAttacked();
        if (!(eToHit instanceof LivingEntity)) return null;
        activateSpecial(getSpec(boss, (LivingEntity) eToHit));
        return null;
    }
}
