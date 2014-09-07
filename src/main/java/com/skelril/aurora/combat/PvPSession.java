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

import com.sk89q.commandbook.session.PersistentSession;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class PvPSession extends PersistentSession {

    public static final long MAX_AGE = TimeUnit.DAYS.toMillis(1);

    // Flag booleans
    private boolean hasPvPOn = false;
    private boolean useSafeSpots = true;

    // Punishment booleans & data
    private boolean wasKicked = false;
    private boolean punishNextLogin = false;

    private long nextFreePoint = 0;

    protected PvPSession() {

        super(MAX_AGE);
    }

    public Player getPlayer() {

        CommandSender sender = super.getOwner();
        return sender instanceof Player ? (Player) sender : null;
    }

    public boolean hasPvPOn() {

        return hasPvPOn;
    }

    public void setPvP(boolean hasPvPOn) {

        this.hasPvPOn = hasPvPOn;
    }

    public boolean useSafeSpots() {

        return useSafeSpots;
    }

    public void useSafeSpots(boolean useSafeSpots) {

        this.useSafeSpots = useSafeSpots;
    }

    public boolean punishNextLogin() {

        return punishNextLogin && !wasKicked;
    }

    public void punishNextLogin(boolean witherNextLogin) {

        this.punishNextLogin = witherNextLogin;
    }

    public void wasKicked(boolean wasKicked) {

        this.wasKicked = wasKicked;
    }

    public boolean recentlyHit() {

        return System.currentTimeMillis() < nextFreePoint;
    }

    public void updateHit() {

        nextFreePoint = System.currentTimeMillis() + 7000;
    }

    public void resetHit() {
        nextFreePoint = 0;
    }
}