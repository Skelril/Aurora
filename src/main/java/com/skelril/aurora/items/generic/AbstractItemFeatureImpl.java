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

package com.skelril.aurora.items.generic;

import com.sk89q.commandbook.CommandBook;
import com.sk89q.commandbook.session.SessionComponent;
import com.skelril.aurora.admin.AdminComponent;
import com.skelril.aurora.events.custom.item.SpecialAttackEvent;
import com.skelril.aurora.items.CustomItemSession;
import com.skelril.aurora.items.specialattack.SpecType;
import com.skelril.aurora.items.specialattack.SpecialAttack;
import com.skelril.aurora.prayer.PrayerComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public abstract class AbstractItemFeatureImpl implements Listener {

    protected static AdminComponent admin;
    protected static SessionComponent sessions;
    protected static PrayerComponent prayers;

    public static void applyResource(AdminComponent admin) {
        AbstractItemFeatureImpl.admin = admin;
    }
    public static void applyResource(SessionComponent sessions) {
        AbstractItemFeatureImpl.sessions = sessions;
    }
    public static void applyResource(PrayerComponent prayers) {
        AbstractItemFeatureImpl.prayers = prayers;
    }

    public CustomItemSession getSession(Player player) {
        return sessions.getSession(CustomItemSession.class, player);
    }

    protected SpecialAttackEvent callSpec(Player owner, ItemStack weapon, SpecType context, SpecialAttack spec) {
        SpecialAttackEvent event = new SpecialAttackEvent(owner, context, weapon, spec);
        CommandBook.server().getPluginManager().callEvent(event);
        return event;
    }
}
