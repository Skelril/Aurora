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

package com.skelril.aurora.city.engine.area.areas.MirageArena;

import com.sk89q.commandbook.session.PersistentSession;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class MirageSession extends PersistentSession {

    private String vote;
    private Set<String> ignored = new HashSet<>();
    private double dmgTaken = 0;

    protected MirageSession() {
        super(TimeUnit.MINUTES.toMillis(30));
    }

    public void vote(String vote) {
        this.vote = vote;
    }

    public String getVote() {
        return vote;
    }

    public boolean isIgnored(String player) {
        return ignored.contains(player);
    }

    public void ignore(String player) {
        ignored.add(player);
    }

    public void unignore(String player) {
        ignored.remove(player);
    }

    public double getDamage() {
        return dmgTaken;
    }

    public void resetDamage() {
        dmgTaken = 0;
    }

    public void addDamage(double amt) {
        dmgTaken += amt;
    }
}
