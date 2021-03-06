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

package com.skelril.aurora.combat;

import com.sk89q.commandbook.CommandBook;
import com.sk89q.commandbook.session.SessionComponent;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.skelril.aurora.prayer.PrayerComponent;
import com.skelril.aurora.util.ChatUtil;
import com.zachsthings.libcomponents.ComponentInformation;
import com.zachsthings.libcomponents.Depend;
import com.zachsthings.libcomponents.InjectComponent;
import com.zachsthings.libcomponents.bukkit.BukkitComponent;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@ComponentInformation(friendlyName = "PvM", desc = "Skelril PvM management.")
@Depend(components = {SessionComponent.class})
public class PvMComponent extends BukkitComponent implements Listener {

    private final static CommandBook inst = CommandBook.inst();
    private final static Logger log = inst.getLogger();
    private final static Server server = CommandBook.server();

    @InjectComponent
    private static SessionComponent sessions;
    @InjectComponent
    private PrayerComponent prayers;

    private static List<PvPScope> pvpLimitors = new ArrayList<>();
    private static WorldGuardPlugin WG;

    @Override
    public void enable() {
        //noinspection AccessStaticViaInstance
        inst.registerEvents(this);
    }

    public static void printHealth(Player player, LivingEntity target) {
        final int oldCurrent = (int) Math.ceil(target.getHealth());

        server.getScheduler().runTaskLater(inst, () -> {

            int current = (int) Math.ceil(target.getHealth());

            if (oldCurrent == current) return;

            PvMSession session = sessions.getSession(PvMSession.class, player);

            int max = (int) Math.ceil(target.getMaxHealth());

            String message;

            if (current > 0) {
                message = ChatColor.DARK_AQUA
                        + String.valueOf(session.checkLast(target.getUniqueId()) ? ChatColor.ITALIC : "")
                        + "Entity Health: " + current + " / " + max;
            } else {
                message = ChatColor.GOLD + String.valueOf(ChatColor.BOLD) + "KO!";
            }

            ChatUtil.send(player, message);
        }, 1);
    }
}