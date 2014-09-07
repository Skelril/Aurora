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

package com.skelril.aurora.jail;

import com.sk89q.commandbook.CommandBook;

import java.util.UUID;

/**
 * Author: Turtle9598
 */
public class Inmate {

    private UUID ID;
    private String name;
    private final String prisonName;
    private final String reason;
    private final long start;
    private final long end;
    private final boolean isMuted;

    public Inmate(String prisonName, String reason, long start, long end, boolean isMuted) {
        this.prisonName = prisonName.trim();
        this.reason = reason.trim();
        this.start = start;
        this.end = end;
        this.isMuted = isMuted;
    }

    public Inmate(UUID ID, String prisonName, String reason, long start, long end, boolean isMuted) {
        this(prisonName, reason, start, end, isMuted);
        this.ID = ID;
    }

    public UUID getID() {
        return ID;
    }

    public void setID(UUID ID) {
        this.ID = ID;
    }

    public String getName() {
        if (name == null || name.isEmpty()) {
            return CommandBook.server().getOfflinePlayer(name).getName();
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrisonName() {
        return prisonName;
    }

    public String getReason() {
        return reason.isEmpty() ? null : reason;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public boolean isMuted() {
        return isMuted;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Inmate)) {
            return false;
        }
        Inmate inmate = (Inmate) other;
        return potentialNullEquals(ID, inmate.ID);
    }

    public static boolean potentialNullEquals(Object a, Object b) {
        return (a == null && b == null)
                || a != null && b != null
                && a.equals(b);
    }

    @Override
    public int hashCode() {
        int result = ID.hashCode();
        result = 32 * result;
        return result;
    }
}
