/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.items.algorithem;

import com.skelril.aurora.events.anticheat.RapidBlockBreakEvent;
import com.skelril.aurora.items.custom.CustomItem;
import com.skelril.aurora.items.custom.CustomItemCenter;
import com.skelril.aurora.items.custom.CustomItems;
import com.skelril.aurora.items.custom.Tag;
import com.skelril.aurora.util.ChatUtil;
import com.skelril.aurora.util.item.ItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

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
        final int dist = getDist(item);
        final int maxDist = getMaxDist(item);
        final short dur = item.getDurability();

        CustomItem cItem = CustomItemCenter.get(itemType);
        for (Tag tag : cItem.getTags()) {
            if (tag.getKey().equals("Distance")) {
                int newDist = dist + 1;
                if (newDist > maxDist) {
                    newDist = 0;
                }
                tag.setProp(String.valueOf(newDist));
                ChatUtil.sendNotice(player, "Distance set to: " + newDist);
            }
        }
        ItemStack result = cItem.build();
        result.setDurability(dur);
        player.setItemInHand(result);
    }

    private void handleLeftClick(Player player, ItemStack item, PlayerInteractEvent event) {

        Block curTarget = event.getClickedBlock();
        final int initialType = curTarget.getTypeId();
        final int initialData = curTarget.getData();

        if (!accepts(initialType, initialData)) return;

        event.setCancelled(true);
        callEvent(new RapidBlockBreakEvent(player));
        final int dist = getDist(item);
        short breaks = 0;
        short curDur = item.getDurability();
        short maxDur = item.getType().getMaxDurability();
        for (int i = 0; i < dist; ++i) {
            if (curTarget.getTypeId() != initialType || curTarget.getData() != initialData) {
                break;
            }

            if (curDur + breaks > maxDur) {
                break;
            }
            if (breakBlock(curTarget, player, item)) {
                ++breaks;
            } else {
                break;
            }
            curTarget = curTarget.getRelative(event.getBlockFace().getOppositeFace());
        }

        if (curDur + breaks >= maxDur) {
            player.setItemInHand(null);
        } else {
            item.setDurability((short) (curDur + breaks));
        }
    }

    private boolean breakBlock(Block b, Player p, ItemStack i) {
        BlockBreakEvent event = new BlockBreakEvent(b, p);
        callEvent(event);
        return !event.isCancelled() && b.breakNaturally(i);
    }
}
