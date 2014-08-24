/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
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
