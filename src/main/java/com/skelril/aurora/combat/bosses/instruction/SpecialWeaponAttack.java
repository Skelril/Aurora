/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
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
