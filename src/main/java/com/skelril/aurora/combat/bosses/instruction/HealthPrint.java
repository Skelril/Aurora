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

import com.skelril.OpenBoss.InstructionResult;
import com.skelril.OpenBoss.condition.DamagedCondition;
import com.skelril.OpenBoss.instruction.DamagedInstruction;
import com.skelril.aurora.combat.PvMComponent;
import com.skelril.aurora.util.extractor.entity.CombatantPair;
import com.skelril.aurora.util.extractor.entity.EDBEExtractor;
import com.skelril.aurora.worlds.WildernessCoreComponent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class HealthPrint implements DamagedInstruction {

    private final InstructionResult<DamagedInstruction> next;

    public HealthPrint() {
        this(null);
    }

    public HealthPrint(InstructionResult<DamagedInstruction> next) {
        this.next = next;
    }

    private static EDBEExtractor<Player, LivingEntity, Projectile> extractor = new EDBEExtractor<>(
            Player.class,
            LivingEntity.class,
            Projectile.class
    );

    @Override
    public InstructionResult<DamagedInstruction> process(DamagedCondition condition) {
        LivingEntity boss = condition.getBoss().getEntity();
        Event e = condition.getEvent();
        if (e instanceof EntityDamageByEntityEvent) {
            CombatantPair<Player, LivingEntity, Projectile> result = extractor.extractFrom((EntityDamageByEntityEvent) e);
            if (result != null) {
                // World is already handled
                if (WildernessCoreComponent.isWildernessWorld(boss.getWorld())) return null;
                PvMComponent.printHealth(result.getAttacker(), boss);
            }
        }
        return next;
    }
}
