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

package com.skelril.aurora.ic.ics;

import com.sk89q.commandbook.CommandBook;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.ic.*;
import org.bukkit.Server;

import java.util.logging.Logger;

public class DelayedRepeater extends AbstractIC {

    private static final CommandBook inst = CommandBook.inst();
    private static final Logger log = inst.getLogger();
    private static final Server server = CommandBook.server();

    private long delay;

    public DelayedRepeater(Server server, ChangedSign block, ICFactory factory) {
        super(server, block, factory);
    }

    @Override
    public void load() {
        try {
            delay = Long.parseLong(getLine(2));
        } catch (Exception ex) {
            delay = 20;
        }
    }

    @Override
    public String getTitle() {

        return "Delay Repeater";
    }

    @Override
    public String getSignTitle() {

        return "DELAY REPEATER";
    }

    @Override
    public void trigger(final ChipState chip) {

        final boolean trigger = chip.getInput(0);
        server.getScheduler().runTaskLater(inst, () -> chip.setOutput(0, trigger), delay);
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new DelayedRepeater(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Delays a current by x ticks.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[]{"delay", ""};
        }
    }
}
