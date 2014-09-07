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

import com.skelril.aurora.modifier.ModifierType;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

import static com.skelril.aurora.modifier.ModifierComponent.getModifierManager;

public abstract class WDropInstruction extends DropInstruction {
    @Override
    public void dropItems(Location target, List<ItemStack> items) {
        if (getModifierManager().isActive(ModifierType.DOUBLE_WILD_DROPS)) {
            items.addAll(items.stream().map(ItemStack::clone).collect(Collectors.toList()));
        }
        super.dropItems(target, items);
    }
}
