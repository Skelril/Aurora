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

package com.skelril.aurora.prayer.PrayerFX;

import com.skelril.aurora.combat.PvPComponent;
import com.skelril.aurora.prayer.PrayerType;
import com.skelril.aurora.util.EntityUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class NecrosisFX extends AbstractEffect {

    private LivingEntity beneficiary = null;

    public void setBeneficiary(LivingEntity beneficiary) {
        this.beneficiary = beneficiary;
    }

    private boolean checkBeneficiary() {
        return beneficiary != null && beneficiary instanceof Player;
    }

    @Override
    public void add(Player player) {
        if (checkBeneficiary() && !PvPComponent.allowsPvP((Player) beneficiary, player)) return;
        EntityUtil.heal(beneficiary, 1);
        EntityUtil.forceDamage(player, 1);
    }

    @Override
    public PrayerType getType() {
        return PrayerType.NECROSIS;
    }
}
