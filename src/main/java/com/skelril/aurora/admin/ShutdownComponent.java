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

package com.skelril.aurora.admin;

import com.sk89q.commandbook.CommandBook;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.skelril.aurora.events.ServerShutdownEvent;
import com.zachsthings.libcomponents.ComponentInformation;
import com.zachsthings.libcomponents.bukkit.BukkitComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitTask;

import java.util.logging.Logger;

@ComponentInformation(friendlyName = "Shudown", desc = "Shutdown system")
public class ShutdownComponent extends BukkitComponent {

    private final CommandBook inst = CommandBook.inst();
    private final Logger log = inst.getLogger();
    private final Server server = CommandBook.server();

    @Override
    public void enable() {

        registerCommands(Commands.class);
    }

    public class Commands {

        @Command(aliases = {"shutdown"}, usage = "[time] [excepted downtime]",
                desc = "Used to restart the server", min = 0, max = 3)
        @CommandPermissions({"aurora.admin.server.shutdown"})
        public void shutdownCmd(CommandContext args, CommandSender sender) throws CommandException {

            int delay = 60;
            String expectedDowntime = "30 seconds";
            if (args.argsLength() > 0) {
                try {
                    delay = Math.min(120, Math.max(10, Integer.parseInt(args.getString(0))));
                    if (args.argsLength() > 1) {
                        expectedDowntime = args.getJoinedStrings(1).trim();
                    }
                } catch (NumberFormatException ex) {
                    throw new CommandException("Invalid time entered!");
                }
            }
            shutdown(delay, expectedDowntime);
        }
    }

    private int seconds = 0;
    private String downTime;
    private BukkitTask task = null;

    public void shutdown(final int assignedSeconds, String expectedDownTime) {

        this.downTime = expectedDownTime;

        if (assignedSeconds > 0) {
            this.seconds = assignedSeconds;
        } else {
            server.shutdown();
            return;
        }

        if (task != null) return;

        // New Task
        task = inst.getServer().getScheduler().runTaskTimer(inst, () -> {
            server.getPluginManager().callEvent(new ServerShutdownEvent(seconds));
            if (seconds > 0 && seconds % 5 == 0 || seconds <= 10 && seconds > 0) {
                Bukkit.broadcastMessage(ChatColor.RED + "Shutting down in " + seconds + " seconds - for "
                        + downTime + " of downtime!");
            } else if (seconds < 1) {
                Bukkit.broadcastMessage(ChatColor.RED + "Shutting down!");
                server.shutdown();
                return;
            }
            seconds -= 1;
        }, 0, 20); // Multiply seconds by 20 to convert to ticks
    }
}
