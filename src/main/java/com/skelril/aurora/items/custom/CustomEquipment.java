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

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;

public class CustomEquipment extends CustomItem {
    public CustomEquipment(CustomItems item, ItemStack base) {
        super(item, base);
    }

    public CustomEquipment(CustomItems item, Material type) {
        this(item, new ItemStack(type));
    }

    public CustomEquipment(CustomEquipment equipment) {
        super(equipment);
    }

    @Override
    public void accept(CustomItemVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ItemStack build() {
        ItemStack stack = super.build();
        ItemMeta meta = stack.getItemMeta();
        if (meta instanceof Repairable) {
            ((Repairable) meta).setRepairCost(400);
        }
        stack.setItemMeta(meta);
        return stack;
    }
}
