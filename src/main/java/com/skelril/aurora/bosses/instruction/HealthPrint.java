/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.bosses.instruction;

import com.skelril.OpenBoss.InstructionResult;
import com.skelril.OpenBoss.condition.DamagedCondition;
import com.skelril.OpenBoss.instruction.DamagedInstruction;
import com.skelril.aurora.city.engine.WildernessCoreComponent;
import com.skelril.aurora.city.engine.combat.PvMComponent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class HealthPrint implements DamagedInstruction {

    private final DamagedInstruction next;

    public HealthPrint() {
        this(null);
    }

    public HealthPrint(DamagedInstruction next) {
        this.next = next;
    }

    @Override
    public InstructionResult<DamagedInstruction> process(DamagedCondition condition) {
        LivingEntity boss = condition.getBoss().getEntity();
        Event e = condition.getEvent();
        if (e instanceof EntityDamageByEntityEvent) {
            Entity attacker = ((EntityDamageByEntityEvent) e).getDamager();
            if (attacker instanceof Player) {
                // World is already handled
                if (WildernessCoreComponent.isWildernessWorld(boss.getWorld())) return null;
                PvMComponent.printHealth((Player) attacker, boss);
            }
        }
        return new InstructionResult<>(next);
    }
}
