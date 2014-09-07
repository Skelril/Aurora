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

package com.skelril.aurora.util.extractor.entity;

import com.skelril.aurora.util.extractor.Extractor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

@SuppressWarnings("unchecked")
public class EDBEExtractor<Attacker extends Entity, Defender extends Entity, Thrown extends Projectile>
       implements Extractor<CombatantPair<Attacker, Defender, Thrown>, EntityDamageByEntityEvent> {

    private final Class<Attacker> attackerType;
    private final Class<Defender> defenderType;
    private final Class<Thrown> thrownClass;

    public EDBEExtractor(Class<Attacker> attackerType, Class<Defender> defenderType, Class<Thrown> thrownClass) {
        this.attackerType = attackerType;
        this.defenderType = defenderType;
        this.thrownClass = thrownClass;
    }

    @Override
    public CombatantPair<Attacker, Defender, Thrown> extractFrom(EntityDamageByEntityEvent event) {
        Entity defender = event.getEntity();
        Entity attacker = event.getDamager();
        Projectile projectile = null;

        if (attacker instanceof Projectile) {
            ProjectileSource shooter = ((Projectile) attacker).getShooter();
            projectile = (Projectile) attacker;
            if (shooter == null || !attackerType.isInstance(shooter) || !thrownClass.isInstance(projectile)) {
                return null;
            }
            attacker = (Entity) shooter;
        }

        if (defenderType.isInstance(defender) && attackerType.isInstance(attacker)) {
            if (projectile == null) {
                return new CombatantPair<>((Attacker) attacker, (Defender) defender);
            }
            return new CombatantPair<>((Attacker) attacker, (Defender) defender, (Thrown) projectile);
        }
        return null;
    }
}
