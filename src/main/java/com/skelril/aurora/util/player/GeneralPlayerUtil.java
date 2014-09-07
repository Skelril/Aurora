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

package com.skelril.aurora.util.player;

import com.google.common.collect.Lists;
import com.sk89q.commandbook.util.InputUtil;
import com.sk89q.minecraft.util.commands.CommandException;
import com.skelril.aurora.util.ChatUtil;
import com.skelril.aurora.util.EnvironmentUtil;
import com.skelril.aurora.util.LocationUtil;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Author: Turtle9598
 */
public class GeneralPlayerUtil {

    /**
     * Make a player state
     */
    public static PlayerState makeComplexState(Player player) {

        return new PlayerState(player.getUniqueId().toString(),
                player.getInventory().getContents(),
                player.getInventory().getArmorContents(),
                player.getHealth(),
                player.getFoodLevel(),
                player.getSaturation(),
                player.getExhaustion(),
                player.getLevel(),
                player.getExp());
    }

    public static List<Player> matchPlayers(Collection<String> players) {
        List<Player> foundPlayers = new ArrayList<>();
        for (String playerName : players) {
            try {
                foundPlayers.add(InputUtil.PlayerParser.matchPlayerExactly(null, playerName));
            } catch (CommandException ignored) {
            }
        }
        return foundPlayers;
    }

    public static void findSafeSpot(Player player) {

        Location toBlock = LocationUtil.findFreePosition(player.getLocation());

        if (toBlock != null && player.teleport(toBlock)) {
            return;
        } else {
            toBlock = player.getLocation();
        }

        Location working = toBlock.clone();

        List<BlockFace> nearbyBlockFaces = Lists.newArrayList(EnvironmentUtil.getNearbyBlockFaces());
        nearbyBlockFaces.remove(BlockFace.SELF);
        Collections.shuffle(nearbyBlockFaces);

        boolean done = false;

        for (int i = 1; i < 10 && !done; i++) {
            for (BlockFace face : nearbyBlockFaces) {
                working = LocationUtil.findFreePosition(toBlock.getBlock().getRelative(face, i).getLocation(working));

                if (working == null) {
                    working = toBlock.clone();
                    continue;
                }

                done = player.teleport(working);
            }
        }

        if (!done) {
            player.teleport(player.getWorld().getSpawnLocation());
            ChatUtil.sendError(player, "Failed to locate a safe location, teleporting to spawn!");
        }
    }

    /**
     * This method is used to hide a player
     *
     * @param player - The player to hide
     * @param to     - The player who can no longer see the player
     * @return - true if change occurred
     */
    public static boolean hide(Player player, Player to) {

        if (to.canSee(player)) {
            to.hidePlayer(player);
            return true;
        }
        return false;
    }

    /**
     * This method is used to show a player
     *
     * @param player - The player to show
     * @param to     - The player who can now see the player
     * @return - true if change occurred
     */
    public static boolean show(Player player, Player to) {

        if (!to.canSee(player)) {
            to.showPlayer(player);
            return true;
        }
        return false;
    }
}
