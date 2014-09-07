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

package com.skelril.aurora.events.anticheat;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class FallBlockerEvent extends PlayerEvent {

    private static final HandlerList handlers = new HandlerList();
    private boolean displayMessage = true;

    public FallBlockerEvent(Player player) {

        super(player);
    }

    public boolean isDisplayingMessage() {

        return displayMessage;
    }

    public void setDisplayMessage(boolean displayMessage) {

        this.displayMessage = displayMessage;
    }

    public HandlerList getHandlers() {

        return handlers;
    }

    public static HandlerList getHandlerList() {

        return handlers;
    }
}
