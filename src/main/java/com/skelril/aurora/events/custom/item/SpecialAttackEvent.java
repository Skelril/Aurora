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

package com.skelril.aurora.events.custom.item;

import com.skelril.aurora.items.specialattack.SpecType;
import com.skelril.aurora.items.specialattack.SpecialAttack;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;

public class SpecialAttackEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    private final ItemStack weapon;
    private final SpecType context;
    private SpecialAttack spec;

    public SpecialAttackEvent(final Player owner, final SpecType context, final ItemStack weapon, final SpecialAttack spec) {

        super(owner);

        Validate.isTrue(owner.equals(spec.getOwner()), "The owner and the spec owner must match!");

        this.weapon = weapon;
        this.context = context;
        this.spec = spec;
    }

    public SpecType getContext() {

        return context;
    }

    public ItemStack getWeapon() {
        return weapon.clone();
    }

    public SpecialAttack getSpec() {

        return spec;
    }

    public void setSpec(SpecialAttack spec) {

        Validate.isTrue(getPlayer().equals(spec.getOwner()), "The owner and the spec owner must match!");

        this.spec = spec;
    }

    public HandlerList getHandlers() {

        return handlers;
    }

    public static HandlerList getHandlerList() {

        return handlers;
    }

    public boolean isCancelled() {

        return cancelled;
    }

    public void setCancelled(boolean cancelled) {

        this.cancelled = cancelled;
    }
}
