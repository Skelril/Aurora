/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.combat.bosses.instruction;

import com.skelril.OpenBoss.Boss;
import com.skelril.OpenBoss.EntityDetail;
import com.skelril.OpenBoss.InstructionResult;
import com.skelril.OpenBoss.condition.DamageCondition;
import com.skelril.OpenBoss.instruction.DamageInstruction;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import static com.sk89q.commandbook.CommandBook.inst;
import static com.zachsthings.libcomponents.bukkit.BasePlugin.server;

public class ThorAttack implements DamageInstruction {

    private final InstructionResult<DamageInstruction> next;

    public ThorAttack() {
        this(null);
    }

    public ThorAttack(InstructionResult<DamageInstruction> next) {
        this.next = next;
    }

    public boolean activate(EntityDetail detail) {
        return true;
    }

    @Override
    public InstructionResult<DamageInstruction> process(DamageCondition condition) {
        Boss boss = condition.getBoss();
        if (activate(boss.getDetail())) {
            Entity bossEnt = boss.getEntity();
            Entity toHit = condition.getAttacked();
            toHit.setVelocity(bossEnt.getLocation().getDirection().multiply(2));

            server().getScheduler().runTaskLater(inst(), () -> {
                Location targetLocation = toHit.getLocation();
                server().getScheduler().runTaskLater(inst(), () -> {
                    targetLocation.getWorld().strikeLightning(targetLocation);
                }, 15);
            }, 30);
        }
        return next;
    }
}
