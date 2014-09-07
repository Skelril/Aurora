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

import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;

public class CombatantPair<Attacker extends Entity, Defender extends Entity, Thrown extends Projectile> {

    private final Attacker attacker;
    private final Defender defender;
    private final Thrown projectile;

    public CombatantPair(Attacker attacker, Defender defender) {
        this(attacker, defender, null);
    }

    public CombatantPair(Attacker attacker, Defender defender, Thrown projectile) {
        this.attacker = attacker;
        this.defender = defender;
        this.projectile = projectile;
    }

    public Attacker getAttacker() {
        return attacker;
    }

    public Defender getDefender() {
        return defender;
    }

    public boolean hasProjectile() {
        return projectile != null;
    }

    public Thrown getProjectile() {
        return projectile;
    }
}
