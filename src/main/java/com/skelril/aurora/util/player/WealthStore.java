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

package com.skelril.aurora.util.player;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public class WealthStore extends GenericWealthStore {

    public WealthStore(String ownerName, ItemStack[] inventoryContents) {

        super(ownerName, inventoryContents);
    }

    public WealthStore(String ownerName, ItemStack[] inventoryContents, ItemStack[] armorContents) {

        super(ownerName, inventoryContents, armorContents);
    }

    public WealthStore(String ownerName, List<ItemStack> itemStacks) {

        super(ownerName, itemStacks);
    }

    public WealthStore(String ownerName, List<ItemStack> itemStacks, int value) {

        super(ownerName, itemStacks, value);
    }

    public WealthStore(String ownerName, int value) {

        super(ownerName, value);
    }
}
