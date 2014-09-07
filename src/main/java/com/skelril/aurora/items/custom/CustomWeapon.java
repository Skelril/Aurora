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

package com.skelril.aurora.items.custom;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public class CustomWeapon extends CustomEquipment {

    private final double damageMod;

    public CustomWeapon(CustomItems item, Material type, double damageMod) {
        super(item, type);
        this.damageMod = damageMod;
        addTag(ChatColor.RED, "Damage Modifier", String.valueOf(damageMod));
    }

    public CustomWeapon(CustomWeapon weapon) {
        super(weapon);
        damageMod = weapon.getDamageMod();
    }

    public double getDamageMod() {
        return damageMod;
    }

    @Override
    public void accept(CustomItemVisitor visitor) {
        visitor.visit(this);
    }
}
