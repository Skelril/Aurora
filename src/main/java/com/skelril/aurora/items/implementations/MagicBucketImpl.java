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

import com.skelril.aurora.items.custom.CustomItems;
import com.skelril.aurora.items.generic.AbstractItemFeatureImpl;
import com.skelril.aurora.util.ChatUtil;
import com.skelril.aurora.util.item.ItemUtil;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import static com.sk89q.commandbook.CommandBook.inst;
import static com.skelril.aurora.anticheat.AntiCheatCompatibilityComponent.*;
import static com.zachsthings.libcomponents.bukkit.BasePlugin.server;

public class MagicBucketImpl extends AbstractItemFeatureImpl {

    public boolean handleRightClick(final Player player) {

        if (admin.isAdmin(player)) return false;

        player.setAllowFlight(!player.getAllowFlight());
        if (player.getAllowFlight()) {
            player.setFlySpeed(.4F);
            exempt(player, PLAYER_FLY);
            ChatUtil.send(player, "The bucket glows brightly.");
        } else {
            player.setFlySpeed(.1F);
            unexempt(player, PLAYER_FLY);
            ChatUtil.send(player, "The power of the bucket fades.");
        }
        return true;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        ItemStack itemStack = event.getItem();

        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            if (ItemUtil.isItem(itemStack, CustomItems.MAGIC_BUCKET) && handleRightClick(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {

        Player player = event.getPlayer();
        ItemStack itemStack = player.getItemInHand();

        if (event.getRightClicked() instanceof Cow && ItemUtil.isItem(itemStack, CustomItems.MAGIC_BUCKET)) {
            server().getScheduler().runTaskLater(inst(), () -> {
                if (!ItemUtil.swapItem(player.getInventory(), CustomItems.MAGIC_BUCKET, CustomItems.MAD_MILK)) {
                    ChatUtil.sendError(player, "Your inventory is too full!");
                    return;
                }
                if (!ItemUtil.hasItem(player, CustomItems.MAGIC_BUCKET)) {
                    if (player.getAllowFlight()) {
                        ChatUtil.send(player, "The power of the bucket fades.");
                    }
                    player.setAllowFlight(false);
                }
            }, 1);
            event.setCancelled(true);
            return;
        }

        if (ItemUtil.isItem(itemStack, CustomItems.MAGIC_BUCKET) && handleRightClick(player)) {
            event.setCancelled(true);
        }
        //noinspection deprecation
        server().getScheduler().runTaskLater(inst(), player::updateInventory, 1);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {

        Player player = event.getPlayer();
        ItemStack itemStack = player.getItemInHand();

        if (ItemUtil.isItem(itemStack, CustomItems.MAGIC_BUCKET) && handleRightClick(player)) {
            event.setCancelled(true);
        }
        //noinspection deprecation
        server().getScheduler().runTaskLater(inst(), player::updateInventory, 1);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {

        final Player player = event.getPlayer();
        ItemStack itemStack = event.getItemDrop().getItemStack();

        if (ItemUtil.isItem(itemStack, CustomItems.MAGIC_BUCKET)) {
            server().getScheduler().runTaskLater(inst(), () -> {
                if (!ItemUtil.hasItem(player, CustomItems.MAGIC_BUCKET)) {
                    if (player.getAllowFlight()) {
                        ChatUtil.send(player, "The power of the bucket fades.");
                    }
                    player.setAllowFlight(false);
                    unexempt(player, PLAYER_FLY);
                }
            }, 1);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onClose(InventoryCloseEvent event) {

        Player player = (Player) event.getPlayer();

        if (!player.getAllowFlight()) return;

        ItemStack[] chestContents = event.getInventory().getContents();
        if (!ItemUtil.findItemOfName(chestContents, CustomItems.MAGIC_BUCKET.toString())) return;

        if (!ItemUtil.hasItem(player, CustomItems.MAGIC_BUCKET)) {
            if (player.getAllowFlight()) {
                ChatUtil.send(player, "The power of the bucket fades.");
            }
            player.setAllowFlight(false);
            unexempt(player, PLAYER_FLY);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerDeath(PlayerDeathEvent event) {

        Player player = event.getEntity();
        ItemStack[] drops = event.getDrops().toArray(new ItemStack[event.getDrops().size()]);

        if (ItemUtil.findItemOfName(drops, CustomItems.MAGIC_BUCKET.toString())) {
            if (player.getAllowFlight()) {
                ChatUtil.send(player, "The power of the bucket fades.");
            }
            player.setAllowFlight(false);
            unexempt(player, PLAYER_FLY);
        }
    }
}
