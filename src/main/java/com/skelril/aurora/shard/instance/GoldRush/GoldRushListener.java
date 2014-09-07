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

package com.skelril.aurora.shard.instance.GoldRush;

import com.sk89q.worldedit.blocks.BlockID;
import com.skelril.aurora.events.PlayerAdminModeChangeEvent;
import com.skelril.aurora.events.PrayerApplicationEvent;
import com.skelril.aurora.events.ServerShutdownEvent;
import com.skelril.aurora.events.shard.PartyActivateEvent;
import com.skelril.aurora.shard.instance.ShardListener;
import com.skelril.aurora.util.ChanceUtil;
import com.skelril.aurora.util.ChatUtil;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;

import static com.sk89q.commandbook.CommandBook.inst;
import static com.zachsthings.libcomponents.bukkit.BasePlugin.server;

public class GoldRushListener extends ShardListener<GoldRush> {
    public GoldRushListener(GoldRush shard) {
        super(shard);
    }

    @EventHandler
    public void onPartyActivate(PartyActivateEvent event) {
        if (!event.hasInstance() && shard.matchesShard(event.getShard())) {
            GoldRushInstance instance = shard.makeInstance();
            instance.teleportTo(shard.wrapPlayers(event.getPlayers()));
            event.setInstance(instance);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        GoldRushInstance inst = shard.getInstance(player);
        if (inst != null && inst.isLocked() && inst.contains(event.getTo()) && !inst.contains(event.getFrom())) {
            event.setCancelled(true);
            ChatUtil.sendWarning(player, "You cannot teleport to that location.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        GoldRushInstance inst = shard.getInstance(player);
        if (inst != null && event.isFlying()) {
            event.setCancelled(true);
            ChatUtil.send(player, "You cannot fly here!");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onAdminModeChange(PlayerAdminModeChangeEvent event) {
        Player player = event.getPlayer();
        GoldRushInstance inst = shard.getInstance(player);
        if (inst != null) {
            switch (event.getNewAdminState()) {
                case MEMBER:
                case SYSOP:
                    return;
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onShutdown(ServerShutdownEvent event) {
        if (event.getSecondsLeft() < 10) {
            shard.clearAndRefund();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPrayerApplication(PrayerApplicationEvent event) {
        GoldRushInstance inst = shard.getInstance(event.getPlayer());
        if (inst != null && event.getCause().getEffect().getType().isHoly()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEvent(PlayerInteractEvent event) {

        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;

        Player player = event.getPlayer();
        GoldRushInstance inst = shard.getInstance(player);
        if (inst == null) return;

        BlockState state = event.getClickedBlock().getLocation().getBlock().getState();
        if (state.getTypeId() == BlockID.WALL_SIGN && inst.getLockLocations().contains(state.getLocation())) {
            Sign sign = (Sign) state;
            if (sign.getLine(1).toLowerCase().contains("blue")) {
                ItemStack blueKey = inst.getBlueKey();
                if (event.getPlayer().getInventory().containsAtLeast(blueKey, 1)) {
                    event.getPlayer().getInventory().removeItem(blueKey);
                    ((Sign) state).setLine(2, "Locked");
                    ((Sign) state).setLine(3, "- Unlocked -");
                    sign.update(true);

                    //noinspection deprecation
                    event.getPlayer().updateInventory();
                }
            } else if (sign.getLine(1).toLowerCase().contains("red")) {
                ItemStack redKey = inst.getRedKey();
                if (event.getPlayer().getInventory().containsAtLeast(redKey, 1)) {
                    event.getPlayer().getInventory().removeItem(redKey);
                    ((Sign) state).setLine(2, "Locked");
                    ((Sign) state).setLine(3, "- Unlocked -");
                    sign.update(true);

                    //noinspection deprecation
                    event.getPlayer().updateInventory();
                }
            }
        } else if (state.getTypeId() == BlockID.LEVER) {
            server().getScheduler().runTaskLater(inst(), () -> {
                if (inst.checkLevers()) inst.unlockLevers();
            }, 1);
        } else if (state.getLocation().equals(inst.getRewardChestLoc()) && inst.isComplete()) {
            event.setUseInteractedBlock(Event.Result.DENY);

            ChatUtil.send(player, "You have successfully robbed the bank!\n");
            ChatUtil.send(player, "[Partner] I've put your split of the money in your account.");
            ChatUtil.send(player, "[Partner] Don't question my logic...\n");
            inst.payPlayer(player);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {

        Player player = event.getEntity();
        GoldRushInstance inst = shard.getInstance(player);
        if (inst == null) return;

        String playerName = event.getEntity().getName();
        String deathMessage;
        switch (ChanceUtil.getRandom(6)) {
            case 1:
                deathMessage = " needs to find a new profession";
                break;
            case 2:
                deathMessage = " is now at the mercy of Hallow";
                break;
            case 3:
                deathMessage = " is now folding and hanging... though mostly hanging...";
                break;
            case 4:
                if (event.getDeathMessage().contains("drown")) {
                    deathMessage = " discovered H2O is not always good for ones health";
                    break;
                }
            case 5:
                if (event.getDeathMessage().contains("starved")) {
                    deathMessage = " should take note of the need to bring food with them";
                    break;
                }
            default:
                deathMessage = " was killed by police while attempting to rob a bank";
                break;
        }
        event.setDeathMessage(playerName + deathMessage);
        ChatUtil.send(player, "Your partner posted bail as promised.");
    }
}
