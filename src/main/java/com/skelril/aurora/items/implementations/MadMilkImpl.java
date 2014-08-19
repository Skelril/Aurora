/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.items.implementations;

import com.skelril.aurora.items.custom.CustomItemCenter;
import com.skelril.aurora.items.custom.CustomItems;
import com.skelril.aurora.items.generic.AbstractItemFeatureImpl;
import com.skelril.aurora.util.item.ItemUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import static com.sk89q.commandbook.CommandBook.inst;
import static com.zachsthings.libcomponents.bukkit.BasePlugin.server;

public class MadMilkImpl extends AbstractItemFeatureImpl {
    @EventHandler(ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {

        Player player = event.getPlayer();
        ItemStack stack = event.getItem();

        if (ItemUtil.isItem(stack, CustomItems.MAD_MILK)) {
            server().getScheduler().runTaskLater(inst(), () -> player.getInventory().setItemInHand(CustomItemCenter.build(CustomItems.MAGIC_BUCKET)), 1);
            event.setCancelled(true);
        }
    }
}
