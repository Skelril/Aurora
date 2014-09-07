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

import com.sk89q.worldedit.blocks.BlockID;
import com.skelril.aurora.prayer.PrayerType;
import com.skelril.aurora.util.ChanceUtil;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Author: Turtle9598
 */
public class InventoryFX extends AbstractEffect {

    private int type, amount;

    public InventoryFX() {
        super();
        this.type = BlockID.DIRT;
        this.amount = 1;
    }

    public InventoryFX(int type, int amount) {
        super();
        this.type = type;
        this.amount = amount;
    }

    @Override
    public PrayerType getType() {

        return PrayerType.INVENTORY;
    }

    @Override
    public void add(Player player) {

        ItemStack held = player.getItemInHand().clone();
        ItemStack stack = new ItemStack(type, ChanceUtil.getRandom(amount));
        if (held != null && held.getTypeId() != BlockID.AIR && !held.isSimilar(stack)) {
            Item item = player.getWorld().dropItem(player.getLocation(), held);
            item.setPickupDelay(20 * 5);
        }
        player.setItemInHand(stack);
    }

    @Override
    public void clean(Player player) {

        // Nothing to do here
    }
}
