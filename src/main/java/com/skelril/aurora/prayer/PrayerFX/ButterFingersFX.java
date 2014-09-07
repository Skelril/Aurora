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
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Author: Turtle9598
 */
public class ButterFingersFX extends AbstractEffect {

    @Override
    public PrayerType getType() {

        return PrayerType.BUTTERFINGERS;
    }

    @Override
    public void add(Player player) {

        for (ItemStack itemStack : player.getInventory().getArmorContents()) {
            if (itemStack != null && itemStack.getTypeId() != BlockID.AIR) {
                player.getWorld().dropItem(player.getLocation(), itemStack.clone());
            }
        }
        for (ItemStack itemStack : player.getInventory().getContents()) {
            if (itemStack != null && itemStack.getTypeId() != BlockID.AIR) {
                player.getWorld().dropItem(player.getLocation(), itemStack.clone());
            }
        }

        player.getInventory().setArmorContents(null);
        player.getInventory().clear();
    }

    @Override
    public void clean(Player player) {

        // Nothing to do here
    }
}
