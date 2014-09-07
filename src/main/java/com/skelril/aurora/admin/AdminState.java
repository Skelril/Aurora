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

package com.skelril.aurora.admin;

import java.util.HashSet;
import java.util.Set;

/**
 * Author: Turtle9598
 */
public enum AdminState {
    MEMBER,
    MODERATOR(MEMBER),
    ADMIN(MODERATOR),
    SYSOP(ADMIN);

    private final AdminState child;
    private Set<AdminState> states = new HashSet<>();

    AdminState() {
        child = null;
    }

    AdminState(AdminState child) {
        this.child = child;
        addState(child);
    }

    private void addState(AdminState state) {
        if (state == null) return;
        states.add(state);
        addState(state.child);
    }

    public boolean isAbove(AdminState state) {
        return this.equals(state) || states.contains(state);
    }
}
