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

package com.skelril.aurora.items.specialattack.attacks.ranged.misc;

import com.skelril.aurora.items.specialattack.LocationAttack;
import com.skelril.aurora.items.specialattack.attacks.ranged.RangedSpecial;
import com.skelril.aurora.util.item.EffectUtil;
import org.bukkit.Location;
import org.bukkit.entity.Bat;
import org.bukkit.entity.LivingEntity;

public class MobAttack extends LocationAttack implements RangedSpecial {

    private Class<? extends LivingEntity> type;

    public <T extends LivingEntity> MobAttack(LivingEntity owner, Location target, Class<T> type) {
        super(owner, target);
        this.type = type;
    }

    @Override
    public void activate() {

        EffectUtil.Strange.mobBarrage(target, type);

        if (Bat.class.equals(type)) {
            inform("Your bow releases a batty attack.");
        } else {
            inform("Your bow releases a " + type.getSimpleName().toLowerCase() + " attack.");
        }
    }
}
