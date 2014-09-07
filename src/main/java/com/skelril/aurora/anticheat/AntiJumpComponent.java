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

package com.skelril.aurora.anticheat;

import com.sk89q.commandbook.CommandBook;
import com.skelril.aurora.util.ChatUtil;
import com.skelril.aurora.util.LocationUtil;
import com.zachsthings.libcomponents.ComponentInformation;
import com.zachsthings.libcomponents.bukkit.BukkitComponent;
import com.zachsthings.libcomponents.config.ConfigurationBase;
import com.zachsthings.libcomponents.config.Setting;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.logging.Logger;

@ComponentInformation(friendlyName = "Anti Jump", desc = "Stop the jump hackers")
public class AntiJumpComponent extends BukkitComponent implements Listener {

    private final CommandBook inst = CommandBook.inst();
    private final Logger log = inst.getLogger();
    private final Server server = CommandBook.server();

    private LocalConfiguration config;

    @Override
    public void enable() {

        config = configure(new LocalConfiguration());
        //noinspection AccessStaticViaInstance
        inst.registerEvents(this);
    }

    @Override
    public void reload() {

        super.reload();
        configure(config);
    }

    private static class LocalConfiguration extends ConfigurationBase {

        @Setting("upwards-velocity")
        public double upwardsVelocity = .1;
        @Setting("leap-distance")
        public double leapDistance = 1.2;
        @Setting("radius")
        public double radius = 2;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {

        if (event.isCancelled()) {

            final Player player = event.getPlayer();
            final Location playerLoc = player.getLocation();

            final Location blockLoc = event.getBlock().getLocation();
            final int blockY = blockLoc.getBlockY();

            if (Math.abs(player.getVelocity().getY()) > config.upwardsVelocity && playerLoc.getY() > blockY) {
                server.getScheduler().runTaskLater(inst, () -> {
                    if (player.getLocation().getY() >= (blockY + config.leapDistance)) {

                        if (LocationUtil.distanceSquared2D(playerLoc, blockLoc) > config.radius * config.radius) {
                            return;
                        }

                        ChatUtil.sendWarning(player, "Hack jumping detected.");

                        playerLoc.setY(blockY);

                        player.teleport(playerLoc, PlayerTeleportEvent.TeleportCause.UNKNOWN);
                    }
                }, 4);
            }
        }
    }
}
