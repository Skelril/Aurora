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

package com.skelril.aurora.util.restoration;

import com.sk89q.commandbook.CommandBook;
import com.sk89q.worldedit.blocks.ItemID;
import com.skelril.aurora.util.EnvironmentUtil;
import com.skelril.aurora.util.item.ItemUtil;
import com.zachsthings.libcomponents.ComponentInformation;
import com.zachsthings.libcomponents.bukkit.BukkitComponent;
import de.diddiz.LogBlock.Consumer;
import de.diddiz.LogBlock.LogBlock;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

@ComponentInformation(friendlyName = "RestorationUtil", desc = "A restoration utility.")
//@Depend(plugins = {"LogBlock"})
public class RestorationUtil extends BukkitComponent {

    private final static CommandBook inst = CommandBook.inst();
    private final Logger log = inst.getLogger();
    private final static Server server = CommandBook.server();

    private LogBlock logBlock = null;

    public void enable() {

        Plugin plugin = server.getPluginManager().getPlugin("LogBlock");
        if (plugin != null && plugin instanceof LogBlock) {
            logBlock = (LogBlock) plugin;
        }
    }

    public void blockAndLogEvent(Cancellable event) {

        event.setCancelled(true);

        Consumer consumer = logBlock.getConsumer();

        if (consumer != null) {

            final Player player;
            final Block block;
            int xp = 0;

            if (event instanceof BlockBreakEvent) {
                BlockBreakEvent aEvent = (BlockBreakEvent) event;
                player = aEvent.getPlayer();
                block = aEvent.getBlock();
                xp = aEvent.getExpToDrop();

                handleToolDamage(player);
            } else if (event instanceof PlayerBucketFillEvent) {
                PlayerBucketFillEvent aEvent = (PlayerBucketFillEvent) event;
                player = aEvent.getPlayer();
                block = aEvent.getBlockClicked();

                final int amt = player.getItemInHand().getAmount();
                final boolean isWater = EnvironmentUtil.isWater(block);
                server.getScheduler().runTaskLater(inst, () -> {

                    if (amt > 1) {
                        player.setItemInHand(new ItemStack(ItemID.BUCKET, amt));
                    } else {
                        player.setItemInHand(null);
                    }

                    if (isWater) {
                        player.getInventory().addItem(new ItemStack(ItemID.WATER_BUCKET));
                    } else {
                        player.getInventory().addItem(new ItemStack(ItemID.LAVA_BUCKET));
                    }
                }, 1);
            } else {
                return;
            }
            consumer.queueBlockBreak(player.getName(), block.getState());

            block.setTypeId(0);

            if (xp > 0) {
                ExperienceOrb orb = block.getWorld().spawn(block.getLocation(), ExperienceOrb.class);
                orb.setExperience(xp);
            }

        }
    }

    public static void handleToolDamage(final Player player) {

        server.getScheduler().runTaskLater(inst, () -> {
            ItemStack held = player.getItemInHand();
            if (!ItemUtil.isTool(held.getTypeId())) return;
            short newDurability = (short) (held.getDurability() + 1);
            short maxDurability = held.getType().getMaxDurability();
            if (newDurability >= maxDurability) {
                player.setItemInHand(null);
            } else {
                held.setDurability(newDurability);
                player.setItemInHand(held);
            }
        }, 1);
    }
}
