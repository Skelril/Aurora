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

package com.skelril.aurora.items.implementations;

import com.skelril.aurora.items.custom.CustomItemCenter;
import com.skelril.aurora.items.custom.CustomItems;
import com.skelril.aurora.items.generic.AbstractCondenserImpl;
import com.skelril.aurora.util.ChatUtil;
import com.skelril.aurora.util.ItemCondenser;
import com.skelril.aurora.util.item.ItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class SummationScrollImpl extends AbstractCondenserImpl {

    public SummationScrollImpl(ItemCondenser condenser) {
        super(condenser);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        ItemStack itemStack = event.getItem();

        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            // Scrolls
            boolean isScrollOfSummation = ItemUtil.isItem(itemStack, CustomItems.SCROLL_OF_SUMMATION);
            if (isScrollOfSummation) {
                ItemStack[] result = condenser.operate(player.getInventory().getContents());
                if (result != null) {
                    player.getInventory().setContents(result);
                    ItemUtil.removeItemOfName(player, CustomItemCenter.build(CustomItems.SCROLL_OF_SUMMATION), 1, false);
                    ChatUtil.send(player, ChatColor.GOLD, "The scroll glows brightly before turning to dust...");
                }
            }
        }
    }
}
