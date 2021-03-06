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

package com.skelril.aurora.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

@SuppressWarnings("SameParameterValue")
public class SignTeleportEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    private final Player player;
    private Location fromLocation;
    private String toLocationName;
    private Location toLocation;
    private boolean useTA = true;
    private final PlayerTeleportEvent.TeleportCause teleportCause;
    private boolean deactivateSignTeleport = true;
    private final PlayerPortalEvent event;

    public SignTeleportEvent(Player player, Location fromLocation, String toLocationName, Location toLocation,
                             PlayerTeleportEvent.TeleportCause teleportCause, PlayerPortalEvent event) {

        this.player = player;
        this.fromLocation = fromLocation.clone();
        this.toLocationName = toLocationName;
        this.toLocation = toLocation;
        this.teleportCause = teleportCause;
        this.event = event;
    }

    public Player getPlayer() {

        return player;
    }

    public Location getFromLocation() {

        return fromLocation;
    }

    public void setFromLocation(Location fromLocation) {

        this.fromLocation = fromLocation;
    }

    public String getToLocationName() {

        return toLocationName;
    }

    public void setToLocationName(String toLocationName) {

        this.toLocationName = toLocationName;
    }

    public Location getToLocation() {

        return toLocation;
    }

    public void setToLocation(Location toLocation) {

        this.toLocation = toLocation;
    }

    public boolean useTravelAgent() {

        return useTA;
    }

    public void setUseTravelAgent(boolean useTA) {

        this.useTA = useTA;
    }

    public PlayerTeleportEvent.TeleportCause getTeleportCause() {

        return teleportCause;
    }

    public boolean getDeactivationAfterUse() {

        return deactivateSignTeleport;
    }

    public void setDeactivateAfterUse(boolean deactivateSignTeleport) {

        this.deactivateSignTeleport = deactivateSignTeleport;
    }

    public PlayerPortalEvent getCause() {

        return event;
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
