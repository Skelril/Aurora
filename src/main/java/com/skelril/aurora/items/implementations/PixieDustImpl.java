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
import com.skelril.aurora.items.generic.AbstractItemFeatureImpl;
import com.skelril.aurora.util.ChatUtil;
import com.skelril.aurora.util.item.ItemUtil;
import com.skelril.aurora.util.timer.IntegratedRunnable;
import com.skelril.aurora.util.timer.TimedRunnable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

import static com.sk89q.commandbook.CommandBook.inst;
import static com.skelril.aurora.anticheat.AntiCheatCompatibilityComponent.*;
import static com.zachsthings.libcomponents.bukkit.BasePlugin.server;

public class PixieDustImpl extends AbstractItemFeatureImpl {

    private List<String> players = new ArrayList<>();

    public boolean handleRightClick(final Player player) {

        if (admin.isAdmin(player)) return false;

        final long currentTime = System.currentTimeMillis();

        if (player.getAllowFlight()) return false;

        if (players.contains(player.getName())) {
            ChatUtil.sendError(player, "You need to wait to regain your faith, and trust.");
            return false;
        }

        player.setAllowFlight(true);
        player.setFlySpeed(.6F);
        exempt(player, PLAYER_FLY);

        ChatUtil.send(player, "You use the Pixie Dust to gain flight.");

        IntegratedRunnable integratedRunnable = new IntegratedRunnable() {
            @Override
            public boolean run(int times) {

                // Just get out of here you stupid players who don't exist!
                if (!player.isValid()) return true;

                if (player.getAllowFlight()) {
                    int c = ItemUtil.countItemsOfName(player.getInventory().getContents(), CustomItems.PIXIE_DUST.toString()) - 1;

                    if (c >= 0) {
                        ItemStack[] pInventory = player.getInventory().getContents();
                        pInventory = ItemUtil.removeItemOfName(pInventory, CustomItems.PIXIE_DUST.toString());
                        player.getInventory().setContents(pInventory);

                        int amount = Math.min(c, 64);
                        while (amount > 0) {
                            player.getInventory().addItem(CustomItemCenter.build(CustomItems.PIXIE_DUST, amount));
                            c -= amount;
                            amount = Math.min(c, 64);
                        }

                        //noinspection deprecation
                        player.updateInventory();

                        if (System.currentTimeMillis() >= currentTime + 13000) {
                            ChatUtil.send(player, "You use some more Pixie Dust to keep flying.");
                        }
                        return false;
                    }
                    ChatUtil.sendWarning(player, "The effects of the Pixie Dust are about to wear off!");
                }
                return true;
            }

            @Override
            public void end() {

                if (player.isValid()) {
                    if (player.getAllowFlight()) {
                        ChatUtil.send(player, "You are no longer influenced by the Pixie Dust.");
                        unexempt(player, PLAYER_FLY);
                    }
                    player.setFallDistance(0);
                    player.setAllowFlight(false);
                    player.setFlySpeed(.1F);
                }
            }
        };

        TimedRunnable runnable = new TimedRunnable(integratedRunnable, 1);
        BukkitTask task = server().getScheduler().runTaskTimer(inst(), runnable, 0, 20 * 15);
        runnable.setTask(task);
        return true;
    }


    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        ItemStack itemStack = event.getItem();

        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            if (ItemUtil.isItem(itemStack, CustomItems.PIXIE_DUST) && handleRightClick(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {

        Player player = event.getPlayer();
        ItemStack itemStack = player.getItemInHand();

        if (ItemUtil.isItem(itemStack, CustomItems.PIXIE_DUST) && handleRightClick(player)) {
            event.setCancelled(true);
        }
        //noinspection deprecation
        server().getScheduler().runTaskLater(inst(), player::updateInventory, 1);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerSneak(PlayerToggleSneakEvent event) {

        Player player = event.getPlayer();

        if (event.isSneaking() && player.getAllowFlight() && player.isOnGround() && !admin.isAdmin(player)) {

            if (player.getFlySpeed() != .6F || !ItemUtil.hasItem(player, CustomItems.PIXIE_DUST)) return;

            player.setAllowFlight(false);
            unexempt(player, PLAYER_FLY);
            ChatUtil.send(player, "You are no longer influenced by the Pixie Dust.");

            final String playerName = player.getName();

            players.add(playerName);

            server().getScheduler().runTaskLater(inst(), () -> players.remove(playerName), 20 * 30);
        }
    }
}
