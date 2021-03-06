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

package com.skelril.aurora.items.algorithem;

import com.skelril.aurora.events.anticheat.RapidBlockBreakEvent;
import com.skelril.aurora.items.custom.CustomItem;
import com.skelril.aurora.items.custom.CustomItemCenter;
import com.skelril.aurora.items.custom.CustomItems;
import com.skelril.aurora.items.custom.Tag;
import com.skelril.aurora.util.ChanceUtil;
import com.skelril.aurora.util.ChatUtil;
import com.skelril.aurora.util.item.ItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

import static com.zachsthings.libcomponents.bukkit.BasePlugin.callEvent;

public abstract class LinearExecutor {

    private CustomItems itemType;

    public LinearExecutor(CustomItems itemType) {
        this.itemType = itemType;
    }

    public abstract boolean accepts(int type, int data);

    public void process(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (!ItemUtil.isItem(item, itemType)) return;

        switch (event.getAction()) {
            case RIGHT_CLICK_BLOCK:
                handleRightClick(player, item);
                break;
            case LEFT_CLICK_BLOCK:
                handleLeftClick(player, item, event);
                break;
        }
    }

    private int getDist(ItemStack item) {
        return Integer.parseInt(ItemUtil.getItemTags(item).get(ChatColor.RED + "Distance"));
    }

    private int getMaxDist(ItemStack item) {
        return Integer.parseInt(ItemUtil.getItemTags(item).get(ChatColor.RED + "Max Distance"));
    }

    private void handleRightClick(Player player, ItemStack item) {

        if (!player.isSneaking()) return;

        final int dist = getDist(item);
        final int maxDist = getMaxDist(item);
        final short dur = item.getDurability();
        Map<Enchantment, Integer> enchants = item.getItemMeta().getEnchants();

        CustomItem cItem = CustomItemCenter.get(itemType);
        for (Tag tag : cItem.getTags()) {
            if (tag.getKey().equals("Distance")) {
                int newDist = dist + 1;
                if (newDist > maxDist) {
                    newDist = 1;
                }
                tag.setProp(String.valueOf(newDist));
                ChatUtil.send(player, "Distance set to: " + newDist);
            }
        }
        ItemStack result = cItem.build();
        result.setDurability(dur);
        ItemMeta meta = result.getItemMeta();
        for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
            meta.addEnchant(entry.getKey(), entry.getValue(), true);
        }
        result.setItemMeta(meta);
        player.setItemInHand(result);
    }

    private void handleLeftClick(Player player, ItemStack item, PlayerInteractEvent event) {

        Block curTarget = event.getClickedBlock();
        final int initialType = curTarget.getTypeId();
        final int initialData = curTarget.getData();

        if (!accepts(initialType, initialData)) return;

        event.setCancelled(true);
        callEvent(new RapidBlockBreakEvent(player));
        short degradation = 0;
        int unbreakingLevel = item.getEnchantmentLevel(Enchantment.DURABILITY);
        short curDur = item.getDurability();
        short maxDur = item.getType().getMaxDurability();
        for (int dist = getDist(item); dist > 0;) {
            if (curTarget.getTypeId() != initialType || curTarget.getData() != initialData) {
                break;
            }

            if (curDur + degradation > maxDur) {
                break;
            }
            if (breakBlock(curTarget, player, item)) {
                if (ChanceUtil.getChance(unbreakingLevel + 1)) {
                    ++degradation;
                }
                --dist;
            } else {
                break;
            }
            curTarget = curTarget.getRelative(event.getBlockFace().getOppositeFace());
        }

        if (curDur + degradation >= maxDur) {
            player.setItemInHand(null);
        } else {
            item.setDurability((short) (curDur + degradation));
        }
    }

    private boolean breakBlock(Block b, Player p, ItemStack i) {
        BlockBreakEvent event = new BlockBreakEvent(b, p);
        callEvent(event);
        return !event.isCancelled() && b.breakNaturally(i);
    }
}
