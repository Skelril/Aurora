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

package com.skelril.aurora.ic;

import com.sk89q.commandbook.CommandBook;
import com.sk89q.craftbook.mechanics.ic.ICManager;
import com.skelril.aurora.ic.ics.DelayedRepeater;
import com.skelril.aurora.ic.ics.GroupSentryGun;
import com.skelril.aurora.ic.ics.NinjaStarSpawner;
import com.zachsthings.libcomponents.ComponentInformation;
import com.zachsthings.libcomponents.Depend;
import com.zachsthings.libcomponents.bukkit.BukkitComponent;
import org.bukkit.Server;

import java.util.logging.Logger;

@ComponentInformation(friendlyName = "IC Component", desc = "Custom ICs!")
@Depend(plugins = "CraftBook")
public class CustomICComponent extends BukkitComponent {

    private static final CommandBook inst = CommandBook.inst();
    private static final Logger log = inst.getLogger();
    private static final Server server = CommandBook.server();

    private ICManager ICCore;

    @Override
    public void enable() {

        server.getScheduler().runTaskLater(inst, () -> {
            ICCore = ICManager.inst();
            if (ICCore == null) {
                log.warning("ICManager is null!");
            }
            registerICs();
        }, 1);

    }

    @SuppressWarnings("AccessStaticViaInstance")
    private void registerICs() {

        ICCore.registerIC("SK1278", "group sentry", new GroupSentryGun.Factory(server), ICCore.familySISO, ICCore.familyAISO);
        ICCore.registerIC("SK9001", "star spawner", new NinjaStarSpawner.Factory(server), ICCore.familySISO, ICCore.familyAISO);
        ICCore.registerIC("SK9002", "delay repeater", new DelayedRepeater.Factory(server), ICCore.familySISO, ICCore.familyAISO);
    }
}
