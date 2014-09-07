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

package com.skelril.aurora.items.specialattack;

import com.sk89q.commandbook.CommandBook;
import com.skelril.aurora.util.ChatUtil;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

public abstract class SpecialAttack {

    protected static final CommandBook inst = CommandBook.inst();
    protected static final Logger log = inst.getLogger();
    protected static final Server server = CommandBook.server();

    protected LivingEntity owner;

    public SpecialAttack(LivingEntity owner) {
        this.owner = owner;
    }

    public abstract void activate();

    public abstract LivingEntity getTarget();

    public abstract Location getLocation();

    public LivingEntity getOwner() {

        return owner;
    }

    protected void inform(String message) {

        if (owner instanceof Player) {
            ChatUtil.send((Player) owner, message);
        }
    }

    public long getCoolDown() {

        return 0;
    }
}
