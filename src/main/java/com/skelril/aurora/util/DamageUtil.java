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

package com.skelril.aurora.util;

import com.sk89q.commandbook.CommandBook;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import static org.bukkit.event.entity.EntityDamageEvent.DamageModifier.BASE;

public class DamageUtil {

    private static final CommandBook inst = CommandBook.inst();
    private static final Logger log = inst.getLogger();
    private static final Server server = CommandBook.server();

    private static Map<Entity, Entity> entries = new ConcurrentHashMap<>();

    public static void damage(LivingEntity attacker, LivingEntity defender, double amount) {

        entries.put(attacker, defender);

        defender.damage(amount, attacker);
    }

    public static boolean remove(Entity attacker, Entity defender) {

        Entity testDefender = entries.remove(attacker);

        return testDefender != null && testDefender.equals(defender);
    }

    public static void multiplyFinalDamage(EntityDamageEvent event, double multiplier) {
        event.setDamage(BASE, Math.max(0, event.getDamage() + (event.getFinalDamage() * (multiplier - 1))));
    }
}
